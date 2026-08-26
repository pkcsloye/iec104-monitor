-- =============================================
-- IEC104 规约日志解析模块 - 数据库初始化脚本
-- =============================================

-- ----------------------------
-- 1、IEC104 日志文件表
-- ----------------------------
drop table if exists iec104_log_file;
create table iec104_log_file (
  file_id           bigint(20)      not null auto_increment    comment '文件ID',
  file_name         varchar(200)    not null                   comment '文件名称',
  file_path         varchar(500)    default ''                 comment '文件路径',
  file_size         bigint(20)      default 0                  comment '文件大小(字节)',
  total_frames      int(11)         default 0                  comment '总帧数',
  total_points      int(11)         default 0                  comment '总数据点数',
  status            char(1)         default '0'                comment '解析状态（0成功 1失败）',
  error_msg         varchar(2000)   default ''                 comment '错误信息',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (file_id)
) engine=innodb auto_increment=1 comment = 'IEC104日志文件表';

-- ----------------------------
-- 2、IEC104 数据点表
-- ----------------------------
drop table if exists iec104_data_point;
create table iec104_data_point (
  id                bigint(20)      not null auto_increment    comment '主键ID',
  file_id           bigint(20)      not null                   comment '文件ID',
  frame_type        char(1)         default ''                 comment '帧类型（S发送 R接收）',
  frame_time        varchar(20)     default ''                 comment '帧时间（HH:MM:SS）',
  frame_seq         int(11)         default 0                  comment '帧序号',
  ioa               int(11)         not null                   comment '信息对象地址IOA',
  raw_value         double          default null               comment '原始测量值（IEEE754浮点）',
  quantity_name     varchar(100)    default ''                 comment '量名称（来自映射配置）',
  quantity_unit     varchar(50)     default ''                 comment '量单位（来自映射配置）',
  primary key (id),
  key idx_file_id (file_id),
  key idx_ioa (ioa),
  key idx_frame_time (frame_time),
  key idx_file_ioa (file_id, ioa)
) engine=innodb auto_increment=1 comment = 'IEC104数据点表';

-- ----------------------------
-- 3、IEC104 IOA映射配置表
-- ----------------------------
drop table if exists iec104_ioa_mapping;
create table iec104_ioa_mapping (
  mapping_id        bigint(20)      not null auto_increment    comment '映射ID',
  ioa               int(11)         not null                   comment '信息对象地址IOA',
  quantity_name     varchar(100)    not null                   comment '量名称（如：A相电压、B相电流）',
  quantity_type     varchar(50)     default ''                 comment '量类型（voltage电压/current电流/power有功功率/reactive无功功率/frequency频率/pf功率因数/other其他）',
  unit              varchar(50)     default ''                 comment '单位（如：kV、A、kW、kVar）',
  description       varchar(500)    default ''                 comment '描述',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (mapping_id),
  unique key uk_ioa (ioa)
) engine=innodb auto_increment=100 comment = 'IEC104 IOA映射配置表';

-- ----------------------------
-- 初始化默认IOA映射（根据常见IEC104点位配置）
-- ----------------------------
insert into iec104_ioa_mapping values(1,  1, 'A相电压',     'voltage',   'kV',  'A相电压测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(2,  3, 'B相电压',     'voltage',   'kV',  'B相电压测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(3,  5, 'C相电压',     'voltage',   'kV',  'C相电压测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(4,  7, 'A相电流',     'current',   'A',   'A相电流测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(5,  9, 'B相电流',     'current',   'A',   'B相电流测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(6, 11, 'C相电流',     'current',   'A',   'C相电流测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(7, 13, '有功功率',     'power',     'kW',  '总有功功率',       '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(8, 15, '无功功率',     'reactive',  'kVar','总无功功率',       '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(9, 19, '功率因数',     'pf',        '',    '功率因数',         '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(10,21, '频率',         'frequency', 'Hz',  '电网频率',         '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(11,25, '线电压AB',     'voltage',   'kV',  'AB线电压',         '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(12,27, '线电压BC',     'voltage',   'kV',  'BC线电压',         '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(13,28, '线电压CA',     'voltage',   'kV',  'CA线电压',         '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(14,37, '零序电流',     'current',   'A',   '零序电流测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(15,38, '零序电压',     'voltage',   'kV',  '零序电压测量值',   '0', 'admin', sysdate(), '', null, '');
insert into iec104_ioa_mapping values(16,42, '有功电能',     'power',     'kWh', '有功电能量',       '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 菜单 SQL（IEC104日志分析模块）
-- ----------------------------
-- 一级菜单：IEC104分析
insert into sys_menu values('2000', 'IEC104分析', '0', '5', 'iec104', null, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '', null, 'IEC104规约日志分析目录');

-- 二级菜单
insert into sys_menu values('2001', '日志管理', '2000', '1', 'log', 'iec104/log/index', '', '', 1, 0, 'C', '0', '0', 'iec104:log:list', 'log', 'admin', sysdate(), '', null, 'IEC104日志管理菜单');
insert into sys_menu values('2002', '数据图表', '2000', '2', 'chart', 'iec104/chart/index', '', '', 1, 0, 'C', '0', '0', 'iec104:chart:list', 'chart', 'admin', sysdate(), '', null, 'IEC104数据图表菜单');
insert into sys_menu values('2003', '点位映射', '2000', '3', 'mapping', 'iec104/mapping/index', '', '', 1, 0, 'C', '0', '0', 'iec104:mapping:list', 'dict', 'admin', sysdate(), '', null, 'IEC104点位映射配置菜单');

-- 日志管理按钮
insert into sys_menu values('2010', '日志查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:log:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2011', '日志上传', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:log:upload', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '日志删除', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:log:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '日志详情', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:log:detail', '#', 'admin', sysdate(), '', null, '');

-- 点位映射按钮
insert into sys_menu values('2020', '映射查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:mapping:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2021', '映射新增', '2003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:mapping:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '映射修改', '2003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:mapping:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2023', '映射删除', '2003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'iec104:mapping:remove', '#', 'admin', sysdate(), '', null, '');
