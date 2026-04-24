CREATE DATABASE IF NOT EXISTS `yujianghuai`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `yujianghuai`;

CREATE TABLE IF NOT EXISTS `sys_tenant` (
    `id` BIGINT NOT NULL COMMENT '租户ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '数据归属租户ID',
    `tenant_code` VARCHAR(64) NOT NULL COMMENT '租户编码',
    `tenant_name` VARCHAR(128) NOT NULL COMMENT '租户名称',
    `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户表';

CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id` BIGINT NOT NULL COMMENT '部门ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
    `ancestors` VARCHAR(512) NOT NULL DEFAULT '0' COMMENT '祖级列表',
    `dept_name` VARCHAR(128) NOT NULL COMMENT '部门名称',
    `dept_code` VARCHAR(64) NOT NULL COMMENT '部门编码',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `leader` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dept_tenant_code` (`tenant_id`, `dept_code`),
    KEY `idx_sys_dept_parent` (`tenant_id`, `parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `sys_post` (
    `id` BIGINT NOT NULL COMMENT '岗位ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `post_code` VARCHAR(64) NOT NULL COMMENT '岗位编码',
    `post_name` VARCHAR(128) NOT NULL COMMENT '岗位名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_post_tenant_code` (`tenant_id`, `post_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(128) NOT NULL COMMENT '登录密码BCrypt',
    `nickname` VARCHAR(64) NOT NULL COMMENT '用户昵称',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `mobile` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0未知 1男 2女',
    `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 0平台 1租户',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_tenant_username` (`tenant_id`, `username`),
    KEY `idx_sys_user_dept` (`tenant_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL COMMENT '角色ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(128) NOT NULL COMMENT '角色名称',
    `role_type` TINYINT NOT NULL DEFAULT 1 COMMENT '角色类型: 0内置 1自定义',
    `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据权限: 1全部 2本租户 3本部门 4本部门及以下 5仅本人 6自定义部门',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_tenant_code` (`tenant_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL COMMENT '菜单ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    `menu_type` VARCHAR(16) NOT NULL COMMENT '类型: CATALOG目录 MENU菜单 BUTTON按钮 API接口',
    `menu_scope` VARCHAR(16) NOT NULL DEFAULT 'ADMIN' COMMENT '菜单归属: ADMIN后台 PORTAL前台',
    `menu_name` VARCHAR(128) NOT NULL COMMENT '菜单名称',
    `permission` VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路由地址',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `redirect` VARCHAR(255) DEFAULT NULL COMMENT '重定向地址',
    `icon` VARCHAR(128) DEFAULT NULL COMMENT '图标',
    `method` VARCHAR(16) DEFAULT NULL COMMENT '接口方法',
    `api_path` VARCHAR(255) DEFAULT NULL COMMENT '接口路径',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示: 0隐藏 1显示',
    `keep_alive` TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存: 0否 1是',
    `external_link` TINYINT NOT NULL DEFAULT 0 COMMENT '是否外链: 0否 1是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_menu_tenant_scope_permission` (`tenant_id`, `menu_scope`, `permission`),
    KEY `idx_sys_menu_parent` (`tenant_id`, `parent_id`),
    KEY `idx_sys_menu_api` (`method`, `api_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role` (`tenant_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu` (`tenant_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS `sys_role_dept` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `dept_id` BIGINT NOT NULL COMMENT '部门ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`role_id`, `dept_id`),
    KEY `idx_sys_role_dept_dept` (`tenant_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色自定义数据权限部门关联表';

CREATE TABLE IF NOT EXISTS `sys_user_post` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '岗位ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`user_id`, `post_id`),
    KEY `idx_sys_user_post_post` (`tenant_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户岗位关联表';

CREATE TABLE IF NOT EXISTS `sys_oauth_client` (
    `id` BIGINT NOT NULL COMMENT '客户端ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `client_id` VARCHAR(128) NOT NULL COMMENT '客户端标识',
    `client_secret` VARCHAR(256) DEFAULT NULL COMMENT '客户端密钥BCrypt',
    `client_name` VARCHAR(128) NOT NULL COMMENT '客户端名称',
    `client_authentication_methods` VARCHAR(512) NOT NULL COMMENT '客户端认证方式, 逗号分隔',
    `authorization_grant_types` VARCHAR(512) NOT NULL COMMENT '授权模式, 逗号分隔',
    `redirect_uris` VARCHAR(1000) DEFAULT NULL COMMENT '回调地址, 逗号分隔',
    `post_logout_redirect_uris` VARCHAR(1000) DEFAULT NULL COMMENT '登出回调地址, 逗号分隔',
    `scopes` VARCHAR(512) NOT NULL COMMENT '授权范围, 逗号分隔',
    `access_token_ttl` INT NOT NULL DEFAULT 7200 COMMENT '访问令牌有效期(秒)',
    `refresh_token_ttl` INT NOT NULL DEFAULT 604800 COMMENT '刷新令牌有效期(秒)',
    `require_authorization_consent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要授权确认',
    `reuse_refresh_tokens` TINYINT NOT NULL DEFAULT 0 COMMENT '是否复用刷新令牌',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_oauth_client_tenant_client` (`tenant_id`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OAuth2客户端表';

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id` BIGINT NOT NULL COMMENT '字典类型ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `dict_name` VARCHAR(128) NOT NULL COMMENT '字典名称',
    `dict_type` VARCHAR(128) NOT NULL COMMENT '字典类型',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dict_type` (`tenant_id`, `dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS `sys_dict_data` (
    `id` BIGINT NOT NULL COMMENT '字典数据ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `dict_type` VARCHAR(128) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(128) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(128) NOT NULL COMMENT '字典值',
    `css_class` VARCHAR(128) DEFAULT NULL COMMENT '样式属性',
    `list_class` VARCHAR(128) DEFAULT NULL COMMENT '表格回显样式',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dict_data` (`tenant_id`, `dict_type`, `dict_value`),
    KEY `idx_sys_dict_data_type` (`tenant_id`, `dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT NOT NULL COMMENT '参数ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `config_name` VARCHAR(128) NOT NULL COMMENT '参数名称',
    `config_key` VARCHAR(128) NOT NULL COMMENT '参数键',
    `config_value` VARCHAR(2000) DEFAULT NULL COMMENT '参数值',
    `built_in` TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置: 0否 1是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_config_key` (`tenant_id`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数表';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` BIGINT NOT NULL COMMENT '登录日志ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    `login_type` VARCHAR(32) DEFAULT NULL COMMENT '登录类型',
    `ipaddr` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '用户代理',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0失败 1成功',
    `message` VARCHAR(512) DEFAULT NULL COMMENT '提示消息',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sys_login_log_username` (`tenant_id`, `username`),
    KEY `idx_sys_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id` BIGINT NOT NULL COMMENT '操作日志ID',
    `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID',
    `title` VARCHAR(128) DEFAULT NULL COMMENT '模块标题',
    `business_type` VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
    `method` VARCHAR(255) DEFAULT NULL COMMENT '方法名称',
    `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方式',
    `oper_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人员',
    `oper_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `oper_ip` VARCHAR(64) DEFAULT NULL COMMENT '主机地址',
    `oper_param` TEXT DEFAULT NULL COMMENT '请求参数',
    `json_result` TEXT DEFAULT NULL COMMENT '返回参数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0失败 1成功',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误消息',
    `cost_time` BIGINT DEFAULT NULL COMMENT '消耗时间(ms)',
    `oper_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sys_oper_log_oper_name` (`tenant_id`, `oper_name`),
    KEY `idx_sys_oper_log_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS `biz_demo` (
    `id` BIGINT NOT NULL COMMENT '演示业务ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `demo_code` VARCHAR(64) NOT NULL COMMENT '业务编码',
    `demo_name` VARCHAR(128) NOT NULL COMMENT '业务名称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_demo_tenant_code` (`tenant_id`, `demo_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='演示业务表';

CREATE TABLE IF NOT EXISTS `biz_user_fund` (
    `id` BIGINT NOT NULL COMMENT '自选基金ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `fund_code` VARCHAR(32) NOT NULL COMMENT '基金代码',
    `fund_name` VARCHAR(128) NOT NULL COMMENT '基金名称',
    `holding_amount` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '持有金额',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '修改人',
    `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_user_fund_user_code` (`tenant_id`, `username`, `fund_code`),
    KEY `idx_biz_user_fund_user` (`tenant_id`, `username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户自选基金表';

INSERT INTO `sys_tenant` (`id`, `tenant_id`, `tenant_code`, `tenant_name`, `contact_name`, `contact_phone`, `status`)
VALUES (1, 0, 'demo', '默认租户', '管理员', '13800000000', 1)
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`), `status` = VALUES(`status`);

INSERT INTO `sys_dept` (`id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `dept_code`, `sort_order`, `leader`, `status`)
VALUES
    (100, 1, 0, '0', '默认公司', 'ROOT', 0, 'admin', 1),
    (101, 1, 100, '0,100', '研发部', 'RD', 1, 'admin', 1)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`), `parent_id` = VALUES(`parent_id`), `status` = VALUES(`status`);

INSERT INTO `sys_post` (`id`, `tenant_id`, `post_code`, `post_name`, `sort_order`, `status`)
VALUES
    (100, 1, 'CEO', '负责人', 0, 1),
    (101, 1, 'DEV', '开发工程师', 1, 1)
ON DUPLICATE KEY UPDATE `post_name` = VALUES(`post_name`), `status` = VALUES(`status`);

INSERT INTO `sys_user` (`id`, `tenant_id`, `dept_id`, `username`, `password`, `nickname`, `real_name`, `email`, `mobile`, `user_type`, `status`)
VALUES
    (1, 1, 100, 'admin', '$2a$10$mj6ElL.M72d7mwTlJmmwQ.eBaHVj19uQEJSZGR.Z33kUBfCWIzYpO', '系统管理员', '系统管理员', 'admin@yujianghuai.local', '13800000000', 0, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `nickname` = VALUES(`nickname`), `status` = VALUES(`status`);

INSERT INTO `sys_role` (`id`, `tenant_id`, `role_code`, `role_name`, `role_type`, `data_scope`, `sort_order`, `status`)
VALUES
    (1, 1, 'ADMIN', '系统管理员', 0, 1, 0, 1),
    (2, 1, 'USER', '普通用户', 1, 5, 1, 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `data_scope` = VALUES(`data_scope`), `status` = VALUES(`status`);

INSERT INTO `sys_menu` (`id`, `tenant_id`, `parent_id`, `menu_type`, `menu_scope`, `menu_name`, `permission`, `path`, `component`, `icon`, `method`, `api_path`, `sort_order`, `visible`, `status`)
VALUES
    (1000, 1, 0, 'CATALOG', 'ADMIN', '系统管理', 'system', '/system', 'Layout', 'setting', NULL, NULL, 1, 1, 1),
    (1100, 1, 1000, 'MENU', 'ADMIN', '用户管理', 'system:user:view', '/system/user', 'system/user/index', 'user', NULL, NULL, 1, 1, 1),
    (1101, 1, 1100, 'BUTTON', 'ADMIN', '用户新增', 'system:user:add', NULL, NULL, NULL, NULL, NULL, 1, 0, 1),
    (1102, 1, 1100, 'BUTTON', 'ADMIN', '用户修改', 'system:user:edit', NULL, NULL, NULL, NULL, NULL, 2, 0, 1),
    (1103, 1, 1100, 'BUTTON', 'ADMIN', '用户删除', 'system:user:delete', NULL, NULL, NULL, NULL, NULL, 3, 0, 1),
    (1200, 1, 1000, 'MENU', 'ADMIN', '角色管理', 'system:role:view', '/system/role', 'system/role/index', 'peoples', NULL, NULL, 2, 1, 1),
    (1201, 1, 1200, 'BUTTON', 'ADMIN', '角色授权', 'system:role:grant', NULL, NULL, NULL, NULL, NULL, 1, 0, 1),
    (1300, 1, 1000, 'MENU', 'ADMIN', '菜单管理', 'system:menu:view', '/system/menu', 'system/menu/index', 'tree-table', NULL, NULL, 3, 1, 1),
    (1400, 1, 1000, 'MENU', 'ADMIN', '租户管理', 'system:tenant:view', '/system/tenant', 'system/tenant/index', 'tenant', NULL, NULL, 4, 1, 1),
    (2000, 1, 0, 'CATALOG', 'ADMIN', '业务管理', 'business', '/business', 'Layout', 'database', NULL, NULL, 2, 1, 1),
    (2100, 1, 2000, 'MENU', 'ADMIN', '演示业务', 'business:demo:view', '/business/demo', 'business/demo/index', 'example', NULL, NULL, 1, 1, 1),
    (2101, 1, 2100, 'API', 'ADMIN', '演示Ping接口', 'business:demo:ping', NULL, NULL, NULL, 'GET', '/api/demo/ping', 1, 0, 1),
    (3000, 1, 0, 'CATALOG', 'ADMIN', '认证中心', 'auth', '/auth', 'Layout', 'lock', NULL, NULL, 3, 1, 1),
    (3100, 1, 3000, 'API', 'ADMIN', 'OAuth2签发令牌', 'auth:token:issue', NULL, NULL, NULL, 'POST', '/oauth2/token', 1, 0, 1),
    (3101, 1, 3000, 'API', 'ADMIN', 'Token校验', 'auth:token:check', NULL, NULL, NULL, 'GET', '/token/check_token', 2, 0, 1),
    (3102, 1, 3000, 'API', 'ADMIN', 'Token注销', 'auth:token:logout', NULL, NULL, NULL, 'DELETE', '/token/logout', 3, 0, 1),
    (4000, 1, 0, 'CATALOG', 'PORTAL', '前台页面', 'portal', '/', 'Layout', 'House', NULL, NULL, 1, 1, 1),
    (4100, 1, 4000, 'MENU', 'PORTAL', '基金首页', 'portal:home:view', '/', 'portal/home', 'DataBoard', NULL, NULL, 1, 1, 1),
    (4101, 1, 4100, 'BUTTON', 'PORTAL', '基金搜索', 'portal:fund:search', NULL, NULL, NULL, NULL, NULL, 1, 0, 1),
    (4102, 1, 4100, 'BUTTON', 'PORTAL', '自选添加', 'portal:watchlist:add', NULL, NULL, NULL, NULL, NULL, 2, 0, 1),
    (4103, 1, 4100, 'BUTTON', 'PORTAL', '持仓修改', 'portal:watchlist:edit', NULL, NULL, NULL, NULL, NULL, 3, 0, 1),
    (4104, 1, 4100, 'BUTTON', 'PORTAL', '自选删除', 'portal:watchlist:delete', NULL, NULL, NULL, NULL, NULL, 4, 0, 1),
    (4105, 1, 4100, 'BUTTON', 'PORTAL', '反馈入口', 'portal:feedback:view', NULL, NULL, NULL, NULL, NULL, 5, 0, 1),
    (4106, 1, 4100, 'API', 'PORTAL', '获取用户信息', 'portal:userinfo:view', NULL, NULL, NULL, 'GET', '/token/userinfo', 6, 0, 1),
    (4107, 1, 4100, 'API', 'PORTAL', '查看自选基金', 'portal:watchlist:view', NULL, NULL, NULL, 'GET', '/api/funds/watchlist', 7, 0, 1)
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `status` = VALUES(`status`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`, `tenant_id`)
VALUES (1, 1, 1)
ON DUPLICATE KEY UPDATE `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `sys_user_post` (`user_id`, `post_id`, `tenant_id`)
VALUES (1, 100, 1)
ON DUPLICATE KEY UPDATE `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`)
SELECT 1, `id`, 1 FROM `sys_menu`
WHERE `id` IN (1000, 1100, 1101, 1102, 1103, 1200, 1201, 1300, 1400, 2000, 2100, 2101, 3000, 3100, 3101, 3102, 4000, 4100, 4101, 4102, 4103, 4104, 4105, 4106, 4107)
ON DUPLICATE KEY UPDATE `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`)
SELECT 2, `id`, 1 FROM `sys_menu`
WHERE `id` IN (4000, 4100, 4101, 4102, 4103, 4104, 4105, 4106, 4107)
ON DUPLICATE KEY UPDATE `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `sys_oauth_client` (
    `id`, `tenant_id`, `client_id`, `client_secret`, `client_name`, `client_authentication_methods`,
    `authorization_grant_types`, `redirect_uris`, `scopes`, `access_token_ttl`, `refresh_token_ttl`,
    `require_authorization_consent`, `reuse_refresh_tokens`, `status`
) VALUES (
    1,
    1,
    'yujianghuai-client',
    '$2a$10$Dqqst3cDtdYyTt2ae6VjVeXNUG3gMcE2Y5eN5EjAh4S9NINZeR9Pa',
    '默认客户端',
    'client_secret_basic,client_secret_post',
    'authorization_code,refresh_token,client_credentials,password',
    'http://127.0.0.1:8080/login/oauth2/code/yujianghuai',
    'openid,profile,api.read,api.write',
    7200,
    604800,
    0,
    0,
    1
) ON DUPLICATE KEY UPDATE
    `client_secret` = VALUES(`client_secret`),
    `authorization_grant_types` = VALUES(`authorization_grant_types`),
    `scopes` = VALUES(`scopes`),
    `status` = VALUES(`status`);

INSERT INTO `sys_dict_type` (`id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `remark`)
VALUES
    (1, 1, '系统状态', 'sys_status', 1, '通用启停状态'),
    (2, 1, '菜单类型', 'sys_menu_type', 1, '目录/菜单/按钮/API')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`), `status` = VALUES(`status`);

INSERT INTO `sys_dict_data` (`id`, `tenant_id`, `dict_type`, `dict_label`, `dict_value`, `sort_order`, `is_default`, `status`)
VALUES
    (1, 1, 'sys_status', '禁用', '0', 0, 0, 1),
    (2, 1, 'sys_status', '启用', '1', 1, 1, 1),
    (3, 1, 'sys_menu_type', '目录', 'CATALOG', 0, 0, 1),
    (4, 1, 'sys_menu_type', '菜单', 'MENU', 1, 0, 1),
    (5, 1, 'sys_menu_type', '按钮', 'BUTTON', 2, 0, 1),
    (6, 1, 'sys_menu_type', '接口', 'API', 3, 0, 1)
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`), `status` = VALUES(`status`);

-- 仅保留系统初始化所需数据；业务表结构保留，但不预置任何业务数据。
INSERT INTO `sys_config` (`id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `built_in`, `status`, `remark`)
VALUES
    (1, 1, '登录验证码开关', 'auth.captcha.enabled', 'false', 1, 1, '预留给验证码登录'),
    (2, 1, '登录错误锁定次数', 'auth.login.max-failures', '5', 1, 1, '预留给账号锁定策略')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `status` = VALUES(`status`);

