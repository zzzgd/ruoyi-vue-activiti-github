CREATE TABLE `workflow_customer_complaint` (
                                               `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                                               `serial_no` varchar(30) NOT NULL DEFAULT '' COMMENT '工单单号',
                                               `type` varchar(10) NOT NULL DEFAULT '' COMMENT '审批类型',
                                               `commit_time` datetime DEFAULT NULL COMMENT '提交时间',
                                               `instance_id` varchar(36) NOT NULL DEFAULT '' COMMENT '流程实例id',
                                               `business_key` varchar(36) NOT NULL DEFAULT '' COMMENT '流程business_key',
                                               `state` tinyint(2) DEFAULT NULL COMMENT '状态',
                                               `remark` varchar(100) DEFAULT NULL COMMENT '描述',
                                               `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
                                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COMMENT='客诉工单工作流'