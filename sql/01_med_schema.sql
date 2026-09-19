-- ----------------------------------------------------------------------------
-- AI 医院药品进销存信息管理系统 —— 数据库总体设计
-- 数据库：MySQL 8.0   字符集：utf8mb4
-- 说明：
--   1. 本脚本包含项目全部业务表设计，其中库存出入库相关表由库存出入库模块写入
--   2. 标注【基础资料】【库存管理】【有效期管理】的表，随本次交付提供增删改查功能
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;

-- ============================ 一、基础资料 ============================

-- 药品分类表（树形结构，供药品信息选择使用）
drop table if exists med_category;
create table med_category (
  category_id     bigint(20)      not null auto_increment    comment '药品分类ID',
  parent_id       bigint(20)      default 0                  comment '父分类ID',
  ancestors       varchar(500)    default ''                 comment '祖级列表',
  category_name   varchar(100)    not null                   comment '分类名称',
  category_code   varchar(64)     default ''                 comment '分类编码',
  med_type        char(1)         default '0'                comment '药品类型（0处方药 1非处方药 2中成药 3其他）',
  order_num       int(4)          default 0                  comment '显示顺序',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb auto_increment=1 comment = '药品分类表';

-- 供应商信息表
drop table if exists med_supplier;
create table med_supplier (
  supplier_id     bigint(20)      not null auto_increment    comment '供应商ID',
  supplier_code   varchar(64)     not null                   comment '供应商编码',
  supplier_name   varchar(200)    not null                   comment '供应商名称',
  contact_person  varchar(64)     default ''                 comment '联系人',
  contact_phone   varchar(20)     default ''                 comment '联系电话',
  address         varchar(255)    default ''                 comment '供应商地址',
  license_no      varchar(100)    default ''                 comment '经营许可证号',
  license_date    date                                       comment '许可证有效期至',
  gsp_status      char(1)         default '1'                comment '资质状态（0过期 1有效 2临近到期）',
  credit_level    char(1)         default 'B'                comment '信用等级（A优 B良 C一般 D差）',
  coop_status     char(1)         default '0'                comment '合作状态（0合作中 1暂停合作 2已终止）',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (supplier_id)
) engine=innodb auto_increment=1 comment = '供应商信息表';

-- 药品信息表（包含库存上下限与近效期预警天数，为预警判断提供规则）
drop table if exists med_info;
create table med_info (
  med_id          bigint(20)      not null auto_increment    comment '药品ID',
  med_code        varchar(64)     not null                   comment '药品编码',
  med_name        varchar(200)    not null                   comment '药品通用名',
  trade_name      varchar(200)    default ''                 comment '商品名',
  category_id     bigint(20)      not null                   comment '药品分类ID',
  supplier_id     bigint(20)      default null               comment '默认供应商ID',
  med_spec        varchar(100)    default ''                 comment '规格',
  dosage_form     varchar(50)     default ''                 comment '剂型（片剂/胶囊/注射液等）',
  unit            varchar(20)     default '盒'               comment '计量单位',
  manufacturer    varchar(200)    default ''                 comment '生产厂家',
  approval_no     varchar(100)    default ''                 comment '批准文号',
  storage_cond    varchar(100)    default ''                 comment '存储条件（常温/冷藏/阴凉）',
  purchase_price  decimal(12,2)   default 0.00               comment '参考进价',
  sale_price      decimal(12,2)   default 0.00               comment '零售价',
  stock_min       int(11)         default 0                  comment '库存下限（低于则预警补货）',
  stock_max       int(11)         default 0                  comment '库存上限（高于则预警积压）',
  warn_days       int(11)         default 90                 comment '临期预警天数',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (med_id),
  unique key uk_med_code (med_code)
) engine=innodb auto_increment=1 comment = '药品信息表';

-- ============================ 二、库存数据（总体设计） ============================

-- 库存主表：出入库、退库、盘点、清理统一更新该表（出入库业务由库存出入库模块写入，盘点与清理提供原子更新语句）
-- 注意：本表只保留 update_by、update_time 两个审计字段，没有 create_by、create_time，
--       Mapper 与业务代码中不要引用 create_by、create_time，否则会报字段不存在。
drop table if exists med_stock;
create table med_stock (
  stock_id        bigint(20)      not null auto_increment    comment '库存ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  total_qty       int(11)         default 0                  comment '库存总数量',
  lock_qty        int(11)         default 0                  comment '锁定数量',
  last_in_time    datetime                                   comment '最近入库时间',
  last_out_time   datetime                                   comment '最近出库时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (stock_id),
  unique key uk_stock_med (med_id)
) engine=innodb auto_increment=1 comment = '药品库存表';

-- 库存批次表：按批号管理数量与有效期（入库时由库存模块写入，本模块负责有效期与过期管理）
drop table if exists med_stock_batch;
create table med_stock_batch (
  batch_id        bigint(20)      not null auto_increment    comment '批次ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  supplier_id     bigint(20)      default null               comment '供应商ID',
  batch_no        varchar(64)     not null                   comment '生产批号',
  produce_date    date                                       comment '生产日期',
  expire_date     date                                       comment '有效期至',
  batch_qty       int(11)         default 0                  comment '批次入库数量',
  remain_qty      int(11)         default 0                  comment '批次剩余数量',
  purchase_price  decimal(12,2)   default 0.00               comment '批次进价',
  in_time         datetime                                   comment '入库时间',
  batch_status    char(1)         default '0'                comment '批次状态（0正常 1临期 2过期 3已清理）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (batch_id),
  key idx_batch_med (med_id),
  key idx_batch_expire (expire_date)
) engine=innodb auto_increment=1 comment = '药品库存批次表';

-- 库存流水表：入库、出库、退库、盘点调整、过期清理均写入该表（由库存出入库模块统一维护）
drop table if exists med_stock_flow;
create table med_stock_flow (
  flow_id         bigint(20)      not null auto_increment    comment '流水ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  batch_id        bigint(20)      default null               comment '批次ID',
  flow_type       char(1)         not null                   comment '业务类型（1入库 2出库 3退库 4盘点调整 5过期清理）',
  change_qty      int(11)         not null                   comment '变动数量（正数为增加，负数为减少）',
  before_qty      int(11)         default 0                  comment '变动前库存',
  after_qty       int(11)         default 0                  comment '变动后库存',
  biz_no          varchar(64)     default ''                 comment '业务单号',
  biz_id          bigint(20)      default null               comment '业务主键',
  operator        varchar(64)     default ''                 comment '操作人',
  flow_time       datetime                                   comment '流水时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (flow_id),
  key idx_flow_med (med_id),
  key idx_flow_time (flow_time)
) engine=innodb auto_increment=1 comment = '库存流水表';

-- ============================ 三、库存盘点 ============================

-- 库存盘点主表
drop table if exists med_stock_check;
create table med_stock_check (
  check_id        bigint(20)      not null auto_increment    comment '盘点ID',
  check_no        varchar(64)     not null                   comment '盘点单号',
  check_name      varchar(200)    not null                   comment '盘点名称',
  check_type      char(1)         default '0'                comment '盘点类型（0全盘 1抽盘 2重点盘点）',
  check_status    char(1)         default '0'                comment '盘点状态（0待盘点 1盘点中 2已盘点 3已审核）',
  check_date      date                                       comment '盘点日期',
  check_user      varchar(64)     default ''                 comment '盘点人',
  check_time      datetime                                   comment '盘点时间',
  audit_by        varchar(64)     default ''                 comment '审核人',
  audit_time      datetime                                   comment '审核时间',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (check_id),
  unique key uk_check_no (check_no)
) engine=innodb auto_increment=1 comment = '库存盘点主表';

-- 库存盘点明细表（记录账面数量、实盘数量与盈亏数量）
drop table if exists med_stock_check_item;
create table med_stock_check_item (
  item_id         bigint(20)      not null auto_increment    comment '盘点明细ID',
  check_id        bigint(20)      not null                   comment '盘点ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  batch_id        bigint(20)      default null               comment '批次ID',
  batch_no        varchar(64)     default ''                 comment '生产批号',
  book_qty        int(11)         default 0                  comment '账面数量',
  real_qty        int(11)         default 0                  comment '实盘数量',
  diff_qty        int(11)         default 0                  comment '盈亏数量（实盘-账面）',
  diff_type       char(1)         default '0'                comment '盈亏类型（0正常 1盘盈 2盘亏）',
  diff_reason     varchar(255)    default ''                 comment '盈亏原因',
  create_time     datetime                                   comment '创建时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (item_id),
  key idx_item_check (check_id)
) engine=innodb auto_increment=1 comment = '库存盘点明细表';

-- ============================ 四、库存预警 ============================

-- 库存预警记录表（依据药品信息的库存上下限与近效期预警天数自动生成）
drop table if exists med_stock_warn;
create table med_stock_warn (
  warn_id         bigint(20)      not null auto_increment    comment '预警ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  batch_id        bigint(20)      default null               comment '批次ID',
  batch_no        varchar(64)     default ''                 comment '生产批号',
  warn_type       char(1)         not null                   comment '预警类型（0库存不足 1库存积压 2近效期 3已过期）',
  warn_level      char(1)         default '0'                comment '预警级别（0提示 1警告 2严重）',
  current_qty     int(11)         default 0                  comment '当前库存数量',
  warn_qty        int(11)         default 0                  comment '预警阈值',
  warn_content    varchar(500)    default ''                 comment '预警内容',
  warn_time       datetime                                   comment '预警时间',
  handle_status   char(1)         default '0'                comment '处理状态（0未处理 1已处理 2已忽略）',
  handle_user     varchar(64)     default ''                 comment '处理人',
  handle_time     datetime                                   comment '处理时间',
  handle_remark   varchar(500)    default ''                 comment '处理说明',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  primary key (warn_id),
  key idx_warn_med (med_id),
  key idx_warn_status (handle_status)
) engine=innodb auto_increment=1 comment = '库存预警记录表';

-- ============================ 五、过期药品管理 ============================

-- 过期药品清理记录表
drop table if exists med_expired_clean;
create table med_expired_clean (
  clean_id        bigint(20)      not null auto_increment    comment '清理ID',
  clean_no        varchar(64)     not null                   comment '清理单号',
  med_id          bigint(20)      not null                   comment '药品ID',
  batch_id        bigint(20)      default null               comment '批次ID',
  batch_no        varchar(64)     default ''                 comment '生产批号',
  expire_date     date                                       comment '有效期至',
  clean_qty       int(11)         default 0                  comment '清理数量',
  clean_type      char(1)         default '1'                comment '清理方式（0退货 1销毁 2报损）',
  clean_reason    varchar(255)    default ''                 comment '清理原因',
  clean_status    char(1)         default '0'                comment '清理状态（0待审核 1已确认 2已驳回）',
  clean_user      varchar(64)     default ''                 comment '申请人',
  clean_time      datetime                                   comment '申请时间',
  audit_by        varchar(64)     default ''                 comment '确认人',
  audit_time      datetime                                   comment '确认时间',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (clean_id),
  unique key uk_clean_no (clean_no)
) engine=innodb auto_increment=1 comment = '过期药品清理记录表';


-- ============================ 六、初始化数据 ============================

-- 药品分类（树形：处方药 / 非处方药 / 中成药 / 生物制品）
insert into med_category values(1, 0, '0', '处方药',   'CAT-RX',  '0', 1, '0', 'admin', sysdate(), '', null, '需凭处方销售的药品');
insert into med_category values(2, 0, '0', '非处方药', 'CAT-OTC', '1', 2, '0', 'admin', sysdate(), '', null, '可自行购买的药品');
insert into med_category values(3, 0, '0', '中成药',   'CAT-TCM', '2', 3, '0', 'admin', sysdate(), '', null, '中成药类');
insert into med_category values(4, 0, '0', '生物制品', 'CAT-BIO', '0', 4, '0', 'admin', sysdate(), '', null, '疫苗、血液制品等');
insert into med_category values(5, 1, '0,1', '抗菌药物', 'CAT-RX-01', '0', 1, '0', 'admin', sysdate(), '', null, '抗生素类处方药');
insert into med_category values(6, 1, '0,1', '心血管用药', 'CAT-RX-02', '0', 2, '0', 'admin', sysdate(), '', null, '降压、降脂等');
insert into med_category values(7, 2, '0,2', '解热镇痛类', 'CAT-OTC-01', '1', 1, '0', 'admin', sysdate(), '', null, '感冒退烧类');

-- 供应商
insert into med_supplier values(1, 'SUP-001', '国药控股江西医药有限公司', '王建国', '0791-86543210', '南昌市青山湖区医药大道 88 号', 'JX-YP-2019-0136', '2027-06-30', '1', 'A', '0', '0', 'admin', sysdate(), '', null, '长期合作供应商');
insert into med_supplier values(2, 'SUP-002', '南昌医药集团股份有限公司', '李萍', '0791-86221001', '南昌市西湖区抚河中路 66 号', 'JX-YP-2020-0278', '2026-12-31', '2', 'B', '0', '0', 'admin', sysdate(), '', null, '资质临近到期需跟进');
insert into med_supplier values(3, 'SUP-003', '华润江中制药集团', '陈伟', '0791-85551234', '南昌市高新区火炬大街 1 号', 'JX-YP-2018-0098', '2028-01-31', '1', 'A', '0', '0', 'admin', sysdate(), '', null, '中成药主要供应商');

-- 药品信息
insert into med_info values(1, 'MED-0001', '阿莫西林胶囊', '再林', 5, 1, '0.25g*24 粒', '胶囊剂', '盒', '珠海联邦制药股份有限公司', '国药准字 H44021351', '常温干燥', 12.50, 18.80, 50, 800, 90, '0', 'admin', sysdate(), '', null, '抗菌药物，需处方');
insert into med_info values(2, 'MED-0002', '苯磺酸氨氯地平片', '络活喜', 6, 2, '5mg*7 片', '片剂', '盒', '辉瑞制药有限公司', '国药准字 H20051001', '常温', 28.00, 38.50, 100, 1200, 120, '0', 'admin', sysdate(), '', null, '高血压常用药');
insert into med_info values(3, 'MED-0003', '布洛芬缓释胶囊', '芬必得', 7, 1, '0.3g*20 粒', '胶囊剂', '盒', '中美天津史克制药有限公司', '国药准字 H10900089', '常温', 15.00, 22.00, 60, 600, 90, '0', 'admin', sysdate(), '', null, '解热镇痛类');
insert into med_info values(4, 'MED-0004', '连花清瘟胶囊', '连花清瘟', 3, 3, '0.35g*24 粒', '胶囊剂', '盒', '石家庄以岭药业股份有限公司', '国药准字 Z20040063', '常温阴凉', 18.00, 26.00, 80, 1000, 60, '0', 'admin', sysdate(), '', null, '中成药');
insert into med_info values(5, 'MED-0005', '重组人胰岛素注射液', '优泌乐', 4, 1, '3ml:300 单位', '注射液', '支', '礼来苏州制药有限公司', '国药准字 S20130019', '2-8℃ 冷藏', 55.00, 78.00, 30, 200, 300, '0', 'admin', sysdate(), '', null, '需冷链存储');
insert into med_info values(6, 'MED-0006', '头孢克肟分散片', '达力芬', 5, 2, '0.1g*12 片', '片剂', '盒', '广州白云山制药股份有限公司', '国药准字 H20050987', '常温', 22.00, 31.00, 40, 500, 90, '0', 'admin', sysdate(), '', null, '抗菌药物');

-- 库存主表（对应上述药品，其中 3 号药库存不足、6 号药库存积压，用于预警演示）
insert into med_stock values(1, 1, 420, 0, sysdate(), sysdate(), 'admin', sysdate(), null);
insert into med_stock values(2, 2, 680, 0, sysdate(), sysdate(), 'admin', sysdate(), null);
insert into med_stock values(3, 3, 35, 0, sysdate(), sysdate(), 'admin', sysdate(), null);
insert into med_stock values(4, 4, 260, 0, sysdate(), sysdate(), 'admin', sysdate(), null);
insert into med_stock values(5, 5, 96, 0, sysdate(), sysdate(), 'admin', sysdate(), null);
insert into med_stock values(6, 6, 720, 0, sysdate(), sysdate(), 'admin', sysdate(), null);

-- 库存批次（含正常、临期、过期三种状态数据）
insert into med_stock_batch values(1, 1, 1, 'AMX20260315', '2026-03-15', '2028-03-14', 300, 220, 12.50, '2026-03-20 09:30:00', '0', 'admin', sysdate(), '', null, null);
insert into med_stock_batch values(2, 1, 1, 'AMX20260820', '2026-08-20', '2028-08-19', 200, 200, 12.80, '2026-08-25 10:10:00', '0', 'admin', sysdate(), '', null, null);
insert into med_stock_batch values(3, 2, 2, 'ALP20251201', '2025-12-01', '2026-11-30', 400, 380, 28.00, '2025-12-06 14:20:00', '1', 'admin', sysdate(), '', null, '临近有效期');
insert into med_stock_batch values(4, 2, 2, 'ALP20260401', '2026-04-01', '2028-03-31', 300, 300, 28.50, '2026-04-08 11:05:00', '0', 'admin', sysdate(), '', null, null);
insert into med_stock_batch values(5, 3, 1, 'IBU20251110', '2025-11-10', '2027-11-09', 60, 35, 15.00, '2025-11-15 15:40:00', '0', 'admin', sysdate(), '', null, null);
insert into med_stock_batch values(6, 5, 1, 'INS20260601', '2026-06-01', '2027-05-31', 120, 96, 55.00, '2026-06-05 08:50:00', '1', 'admin', sysdate(), '', null, '冷链药品，临近有效期');
insert into med_stock_batch values(7, 4, 3, 'LHQ20250901', '2025-09-01', '2026-08-31', 150, 60, 18.00, '2025-09-06 09:00:00', '2', 'admin', sysdate(), '', null, '已过期，待清理');
insert into med_stock_batch values(8, 3, 1, 'IBU20240501', '2024-05-01', '2026-04-30', 40, 0, 14.80, '2024-05-06 09:30:00', '3', 'admin', sysdate(), '', null, '已清理');
insert into med_stock_batch values(9, 6, 2, 'CFX20260410', '2026-04-10', '2028-04-09', 400, 120, 22.00, '2026-04-15 16:00:00', '0', 'admin', sysdate(), '', null, null);
insert into med_stock_batch values(10, 6, 2, 'CFX20260905', '2026-09-05', '2028-09-04', 600, 600, 22.20, '2026-09-10 09:15:00', '0', 'admin', sysdate(), '', null, null);

-- 库存待处理预警（库存不足、库存积压、近效期、已过期各一条）
insert into med_stock_warn values(1, 3, null, '', '0', '1', 35, 60, '药品【布洛芬缓释胶囊】当前库存 35 盒，低于库存下限 60，建议及时补货', sysdate(), '0', '', null, '', 'admin', sysdate());
insert into med_stock_warn values(2, 6, null, '', '1', '0', 720, 500, '药品【头孢克肟分散片】当前库存 720 盒，高于库存上限 500，注意库存积压', sysdate(), '0', '', null, '', 'admin', sysdate());
insert into med_stock_warn values(3, 2, 3, 'ALP20251201', '2', '1', 380, 76, '药品【苯磺酸氨氯地平片】批号 ALP20251201 有效期至 2026-11-30，剩余 76 天，属于近效期药品，请优先使用', sysdate(), '0', '', null, '', 'admin', sysdate());
insert into med_stock_warn values(4, 4, 7, 'LHQ20250901', '3', '2', 60, 0, '药品【连花清瘟胶囊】批号 LHQ20250901 已于 2026-08-31 过期，剩余 60 盒 待清理', sysdate(), '0', '', null, '', 'admin', sysdate());

-- 过期药品清理记录（一条待审核）
insert into med_expired_clean values(1, 'CL202609150001', 4, 7, 'LHQ20250901', '2026-08-31', 60, '1', '药品已超过有效期，按规定销毁处理', '0', 'admin', sysdate(), '', null, 'admin', sysdate(), '', null, '待药房负责人确认');
