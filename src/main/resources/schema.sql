CREATE TABLE IF NOT EXISTS product_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    operator_id BIGINT,
    action_type VARCHAR(50),
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE mall_order ADD COLUMN delivery_company VARCHAR(255) DEFAULT NULL COMMENT '快递公司';
ALTER TABLE mall_order ADD COLUMN delivery_no VARCHAR(255) DEFAULT NULL COMMENT '快递单号';

-- Points System Updates
ALTER TABLE sys_user ADD COLUMN current_points INT DEFAULT 0 COMMENT '当前积分';
ALTER TABLE sys_user ADD COLUMN continuous_sign_days INT DEFAULT 0 COMMENT '连续签到天数';
ALTER TABLE sys_user ADD COLUMN last_sign_date DATE DEFAULT NULL COMMENT '最后签到日期';

ALTER TABLE mall_order ADD COLUMN points_discount DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额';
ALTER TABLE mall_order ADD COLUMN pay_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '实付金额';
ALTER TABLE mall_order ADD COLUMN used_points INT DEFAULT 0 COMMENT '使用积分数量';

CREATE TABLE IF NOT EXISTS daily_sign_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sign_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, sign_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS points_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_point INT NOT NULL COMMENT '变动积分',
    reason VARCHAR(255) COMMENT '变动原因',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail_address VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS home_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL COMMENT 'HOME_CAROUSEL, NEWS_CAROUSEL, NOTICE, HIGHLIGHT',
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    content TEXT,
    image_url VARCHAR(512),
    link_url VARCHAR(512),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1 COMMENT '1=Active, 0=Disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inheritor_level_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_level VARCHAR(50),
    apply_level VARCHAR(50),
    reason TEXT,
    proof_materials TEXT,
    status INT DEFAULT 0 COMMENT '0-审核中, 1-通过, 2-拒绝',
    audit_remark TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- IM System Tables
CREATE TABLE IF NOT EXISTS im_friend (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    remark VARCHAR(100) COMMENT '备注名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

CREATE TABLE IF NOT EXISTS im_friend_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_id BIGINT NOT NULL COMMENT '申请人ID',
    to_id BIGINT NOT NULL COMMENT '被申请人ID',
    reason VARCHAR(255) COMMENT '申请理由',
    status INT DEFAULT 0 COMMENT '0待处理, 1已通过, 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

CREATE TABLE IF NOT EXISTS im_chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_id BIGINT NOT NULL COMMENT '发送者ID',
    to_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type INT DEFAULT 0 COMMENT '0文本, 1图片, 2撤回提示',
    is_recalled INT DEFAULT 0 COMMENT '0否, 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
