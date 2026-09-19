-- 胡振鹏负责模块：入库、出库、退库业务单扩展表
-- 只新增业务表，不修改 01_med_schema.sql 中已有的基础资料与库存表。

SET NAMES utf8mb4;

create table if not exists med_stock_order (
  order_id        bigint(20)      not null auto_increment    comment '业务单ID',
  order_no        varchar(64)     not null                   comment '业务单号',
  order_type      char(1)         not null                   comment '业务类型（1入库 2出库 3退库）',
  order_date      date            not null                   comment '业务日期',
  supplier_id     bigint(20)      default null               comment '供应商ID（入库）',
  department      varchar(100)    default ''                 comment '领用/退回科室',
  order_status    char(1)         default '0'                comment '状态（0草稿 1已确认）',
  total_qty       int(11)         default 0                  comment '总数量',
  total_amount    decimal(14,2)   default 0.00               comment '总金额',
  operator        varchar(64)     default ''                 comment '经办人',
  confirm_by      varchar(64)     default ''                 comment '确认人',
  confirm_time    datetime                                   comment '确认时间',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (order_id),
  unique key uk_stock_order_no (order_no),
  key idx_stock_order_type_date (order_type, order_date)
) engine=innodb auto_increment=1 comment='药品库存业务单';

create table if not exists med_stock_order_item (
  item_id         bigint(20)      not null auto_increment    comment '明细ID',
  order_id        bigint(20)      not null                   comment '业务单ID',
  med_id          bigint(20)      not null                   comment '药品ID',
  batch_id        bigint(20)      default null               comment '库存批次ID',
  batch_no        varchar(64)     default ''                 comment '生产批号',
  produce_date    date                                       comment '生产日期',
  expire_date     date                                       comment '有效期至',
  quantity        int(11)         not null                   comment '业务数量',
  unit_price      decimal(12,2)   default 0.00               comment '单价',
  amount          decimal(14,2)   default 0.00               comment '金额',
  primary key (item_id),
  key idx_stock_order_item_order (order_id),
  key idx_stock_order_item_med (med_id),
  key idx_stock_order_item_batch (batch_id)
) engine=innodb auto_increment=1 comment='药品库存业务单明细';
