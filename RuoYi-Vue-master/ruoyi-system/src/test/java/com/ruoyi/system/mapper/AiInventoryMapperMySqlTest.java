package com.ruoyi.system.mapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import com.ruoyi.system.ai.tool.AiInventoryTools;
import com.ruoyi.system.domain.ai.AiInventoryMonthlyMetrics;
import com.ruoyi.system.domain.ai.AiDemandSnapshot;
import com.ruoyi.system.domain.ai.AiExpiredCleanupCandidate;
import com.ruoyi.system.domain.ai.AiExpiryBatch;
import com.ruoyi.system.domain.ai.AiInventoryOperationsBrief;
import com.ruoyi.system.domain.ai.AiInventoryOverview;
import com.ruoyi.system.domain.ai.AiMedicineStock;
import com.ruoyi.system.domain.ai.AiOutboundRanking;
import com.ruoyi.system.domain.ai.AiReplenishmentAdvice;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "RUN_AI_DB_INTEGRATION", matches = "true")
class AiInventoryMapperMySqlTest
{
    private static SqlSessionFactory sessionFactory;

    @BeforeAll
    static void prepareDatabase() throws Exception
    {
        String url = System.getenv().getOrDefault("AI_TEST_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/ai_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
        String user = System.getenv().getOrDefault("AI_TEST_DB_USER", "root");
        String password = System.getenv().getOrDefault("AI_TEST_DB_PASSWORD", "root");
        try (Connection connection = DriverManager.getConnection(url, user, password);
                Statement statement = connection.createStatement())
        {
            statement.execute("drop table if exists med_expired_clean, med_stock_batch, med_stock_flow, med_stock, med_supplier, med_category, med_info");
            statement.execute("create table med_info (med_id bigint primary key, med_code varchar(64), med_name varchar(200), trade_name varchar(200), category_id bigint, med_spec varchar(100), unit varchar(20), manufacturer varchar(200), approval_no varchar(100), storage_cond varchar(100), stock_min int, stock_max int, warn_days int, status char(1))");
            statement.execute("create table med_category (category_id bigint primary key, category_name varchar(200))");
            statement.execute("create table med_supplier (supplier_id bigint primary key, supplier_name varchar(200))");
            statement.execute("create table med_stock (stock_id bigint primary key, med_id bigint, total_qty int, lock_qty int)");
            statement.execute("create table med_stock_batch (batch_id bigint primary key, med_id bigint, supplier_id bigint, batch_no varchar(64), expire_date date, remain_qty int)");
            statement.execute("create table med_expired_clean (clean_id bigint primary key, batch_id bigint, clean_status char(1))");
            statement.execute("create table med_stock_flow (flow_id bigint primary key, med_id bigint, flow_type char(1), change_qty int, flow_time datetime)");
            statement.execute("insert into med_info values (1,'M001','阿莫西林胶囊','',1,'0.25g','盒','测试药厂','H001','常温',50,500,90,'0')");
            statement.execute("insert into med_category values (1,'抗菌药物')");
            statement.execute("insert into med_supplier values (1,'测试供应商')");
            statement.execute("insert into med_stock values (1,1,20,0)");
            statement.execute("insert into med_stock_batch values (1,1,1,'B001','2020-01-01',8)");
            statement.execute("insert into med_stock_batch values (2,1,1,'B002',date_add(curdate(), interval 30 day),12)");
            statement.execute("insert into med_expired_clean values (1,1,'0')");
            statement.execute("insert into med_stock_flow values (1,1,'1',999,'2026-07-31 23:59:59')");
            statement.execute("insert into med_stock_flow values (2,1,'1',100,'2026-08-01 00:00:00')");
            statement.execute("insert into med_stock_flow values (3,1,'2',-30,'2026-08-31 23:59:59')");
            statement.execute("insert into med_stock_flow values (4,1,'3',999,'2026-09-01 00:00:00')");
        }

        UnpooledDataSource dataSource = new UnpooledDataSource("com.mysql.cj.jdbc.Driver", url, user, password);
        Configuration configuration = new Configuration(
                new Environment("ai-test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(false);
        configuration.addMapper(AiInventoryMapper.class);
        String resource = "mapper/system/AiInventoryMapper.xml";
        try (InputStream input = new ClassPathResource(resource).getInputStream())
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void shouldUseHalfOpenMonthBoundaryAndReturnCleanupState()
    {
        try (SqlSession session = sessionFactory.openSession())
        {
            AiInventoryMapper mapper = session.getMapper(AiInventoryMapper.class);
            AiInventoryMonthlyMetrics metrics = mapper.selectMonthlyMetrics(
                    LocalDateTime.of(2026, 8, 1, 0, 0),
                    LocalDateTime.of(2026, 9, 1, 0, 0));

            assertThat(metrics.getInboundQty()).isEqualTo(100L);
            assertThat(metrics.getOutboundQty()).isEqualTo(30L);
            assertThat(metrics.getReturnQty()).isZero();
            assertThat(metrics.getMovementCount()).isEqualTo(2L);
            assertThat(metrics.getLowStockCount()).isEqualTo(1L);
            assertThat(mapper.selectMonthlyTopOutbound(
                    LocalDateTime.of(2026, 8, 1, 0, 0),
                    LocalDateTime.of(2026, 9, 1, 0, 0), 5).get(0).getOutboundQty()).isEqualTo(30L);
            AiExpiredCleanupCandidate cleanup = mapper.selectExpiredCleanupCandidates(10).get(0);
            assertThat(cleanup.getMedId()).isEqualTo(1L);
            assertThat(cleanup.getBatchId()).isEqualTo(1L);
            assertThat(cleanup.getBatchNo()).isEqualTo("B001");
            assertThat(cleanup.getExpiredDays()).isPositive();
            assertThat(cleanup.getRemainQty()).isEqualTo(8);
            assertThat(cleanup.getSupplierName()).isEqualTo("测试供应商");
            assertThat(cleanup.getHasPendingClean()).isTrue();
            assertThat(cleanup.getPendingCleanId()).isEqualTo(1L);
            assertThat(cleanup.getSuggestedAction()).contains("人工审核");
        }
    }

    @Test
    void shouldMapEveryInventoryProjectionWithoutGlobalCamelCaseSetting()
    {
        try (SqlSession session = sessionFactory.openSession())
        {
            AiInventoryMapper mapper = session.getMapper(AiInventoryMapper.class);

            AiExpiryBatch expiring = mapper.selectExpiringBatches(60, 10).get(0);
            assertThat(expiring.getMedId()).isEqualTo(1L);
            assertThat(expiring.getMedCode()).isEqualTo("M001");
            assertThat(expiring.getMedName()).isEqualTo("阿莫西林胶囊");
            assertThat(expiring.getBatchNo()).isEqualTo("B002");
            assertThat(expiring.getExpireDate())
                    .isEqualTo(LocalDate.now(ZoneId.of("Asia/Shanghai")).plusDays(30));
            assertThat(expiring.getRemainDays()).isBetween(29L, 30L);
            assertThat(expiring.getRemainQty()).isEqualTo(12L);
            assertThat(expiring.getSupplierName()).isEqualTo("测试供应商");

            AiInventoryOverview overview = mapper.selectInventoryOverview();
            assertThat(overview.getMedicineCount()).isEqualTo(1L);
            assertThat(overview.getStockQty()).isEqualTo(20L);
            assertThat(overview.getLowStockCount()).isEqualTo(1L);
            assertThat(overview.getOverstockCount()).isZero();
            assertThat(overview.getExpiringBatchCount()).isEqualTo(1L);
            assertThat(overview.getExpiredBatchCount()).isEqualTo(1L);

            AiDemandSnapshot demand = mapper.selectDemandSnapshots(10).get(0);
            assertThat(demand.getMedId()).isEqualTo(1L);
            assertThat(demand.getCurrentQty()).isEqualTo(20);
            assertThat(demand.getAvailableQty()).isEqualTo(12);
            assertThat(demand.getStockMin()).isEqualTo(50);
            assertThat(demand.getStockMax()).isEqualTo(500);

            AiMedicineStock stock = mapper.searchMedicineStock("M001", 10).get(0);
            assertThat(stock.getStockQty()).isEqualTo(20);
            assertThat(stock.getAvailableQty()).isEqualTo(12);
            assertThat(stock.getStockStatus()).isEqualTo("库存不足");
            assertThat(mapper.selectLowStock(10).get(0).getMedId()).isEqualTo(1L);

            AiReplenishmentAdvice advice = new AiInventoryTools(mapper)
                    .listReplenishmentAdvice(30, 10).get(0);
            assertThat(advice.getCurrentQty()).isEqualTo(20);
            assertThat(advice.getAvailableQty()).isEqualTo(12);
            assertThat(advice.getSuggestedOrderQty()).isEqualTo(38);

            AiInventoryOperationsBrief brief = mapper.selectOperationsBrief(36500);
            assertThat(brief.getInboundQty()).isEqualTo(1099L);
            assertThat(brief.getOutboundQty()).isEqualTo(30L);
            assertThat(brief.getReturnQty()).isEqualTo(999L);
            assertThat(brief.getMovementCount()).isEqualTo(4L);
            assertThat(brief.getLowStockCount()).isEqualTo(1L);
            assertThat(brief.getExpiringBatchCount()).isEqualTo(1L);
            assertThat(brief.getExpiredBatchCount()).isEqualTo(1L);

            AiOutboundRanking ranking = mapper.selectTopOutbound(36500, 10).get(0);
            assertThat(ranking.getMedId()).isEqualTo(1L);
            assertThat(ranking.getMedName()).isEqualTo("阿莫西林胶囊");
            assertThat(ranking.getOutboundQty()).isEqualTo(30L);
            assertThat(ranking.getOutboundTransactions()).isEqualTo(1L);
        }
    }
}
