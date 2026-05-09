-- ============================================================
-- AI 原生校园学业就业智能服务平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS acaj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE acaj;

-- ============================================================
-- 1. 系统管理模块
-- ============================================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    user_type   TINYINT      NOT NULL DEFAULT 0 COMMENT '用户类型: 0-学生 1-教师 2-管理员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码',
    remark      VARCHAR(200) DEFAULT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户-角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单/权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    path        VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    menu_type   TINYINT      NOT NULL DEFAULT 1 COMMENT '类型: 1-菜单 2-按钮',
    permission  VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色-菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================
-- 2. 学业管理模块
-- ============================================================

-- 学生学业画像
DROP TABLE IF EXISTS academic_student;
CREATE TABLE academic_student (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '关联 sys_user.id',
    student_no      VARCHAR(30)  NOT NULL COMMENT '学号',
    major           VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade           VARCHAR(20)  DEFAULT NULL COMMENT '年级(如2024级)',
    class_name      VARCHAR(50)  DEFAULT NULL COMMENT '班级',
    gpa             DECIMAL(4,2) DEFAULT 0.00 COMMENT '绩点',
    total_credits   DECIMAL(5,1) DEFAULT 0.0 COMMENT '已获总学分',
    required_credits DECIMAL(5,1) DEFAULT 0.0 COMMENT '应修学分',
    fail_count      INT          DEFAULT 0 COMMENT '不及格科目数',
    advisor         VARCHAR(50)  DEFAULT NULL COMMENT '辅导员',
    enrollment_year INT          DEFAULT NULL COMMENT '入学年份',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学业画像表';

-- 课程信息表
DROP TABLE IF EXISTS academic_course;
CREATE TABLE academic_course (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    course_no   VARCHAR(30)  NOT NULL COMMENT '课程编号',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit      DECIMAL(3,1) NOT NULL COMMENT '学分',
    course_type TINYINT      DEFAULT 1 COMMENT '类型: 1-必修 2-选修 3-公选',
    semester    VARCHAR(20)  DEFAULT NULL COMMENT '开课学期',
    teacher     VARCHAR(50)  DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

-- 成绩记录表
DROP TABLE IF EXISTS academic_grade;
CREATE TABLE academic_grade (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL COMMENT '关联 academic_student.id',
    course_id   BIGINT       NOT NULL COMMENT '关联 academic_course.id',
    score       DECIMAL(5,2) DEFAULT NULL COMMENT '分数',
    grade_point DECIMAL(3,2) DEFAULT NULL COMMENT '绩点',
    passed      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否通过',
    semester    VARCHAR(20)  DEFAULT NULL COMMENT '学期',
    exam_type   TINYINT      DEFAULT 1 COMMENT '考试类型: 1-正考 2-补考 3-重修',
    remark      VARCHAR(200) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩记录表';

-- 学业预警表
DROP TABLE IF EXISTS academic_warning;
CREATE TABLE academic_warning (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    student_id    BIGINT       NOT NULL COMMENT '关联 academic_student.id',
    warning_type  TINYINT      NOT NULL COMMENT '预警类型: 1-挂科预警 2-绩点预警 3-学分预警 4-出勤预警',
    warning_level TINYINT      NOT NULL DEFAULT 1 COMMENT '预警等级: 1-黄色 2-橙色 3-红色',
    title         VARCHAR(100) NOT NULL COMMENT '预警标题',
    description   TEXT         DEFAULT NULL COMMENT '预警详情',
    is_read       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读',
    is_handled    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已处理',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_warning (student_id, warning_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学业预警表';

-- 个性化学习规划表
DROP TABLE IF EXISTS academic_study_plan;
CREATE TABLE academic_study_plan (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL COMMENT '关联 academic_student.id',
    plan_title  VARCHAR(100) NOT NULL COMMENT '规划标题',
    semester    VARCHAR(20)  NOT NULL COMMENT '目标学期',
    plan_detail TEXT         DEFAULT NULL COMMENT '规划详情(JSON)',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已放弃',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_plan (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习规划表';

-- ============================================================
-- 3. 就业服务模块
-- ============================================================

-- 简历表
DROP TABLE IF EXISTS employ_resume;
CREATE TABLE employ_resume (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    student_id    BIGINT       NOT NULL COMMENT '关联 academic_student.id',
    title         VARCHAR(100) NOT NULL COMMENT '简历标题',
    content       MEDIUMTEXT   DEFAULT NULL COMMENT '简历内容(JSON格式,包含教育/实习/技能等)',
    target_job    VARCHAR(100) DEFAULT NULL COMMENT '目标岗位',
    target_city   VARCHAR(50)  DEFAULT NULL COMMENT '目标城市',
    ai_score      INT          DEFAULT NULL COMMENT 'AI评分',
    ai_suggestion TEXT         DEFAULT NULL COMMENT 'AI优化建议',
    is_default    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认简历',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_resume (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- 招聘岗位表
DROP TABLE IF EXISTS employ_job;
CREATE TABLE employ_job (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    job_title       VARCHAR(100) NOT NULL COMMENT '岗位名称',
    company         VARCHAR(100) NOT NULL COMMENT '公司名称',
    salary_range    VARCHAR(50)  DEFAULT NULL COMMENT '薪资范围',
    city            VARCHAR(50)  DEFAULT NULL COMMENT '工作城市',
    education       VARCHAR(20)  DEFAULT NULL COMMENT '学历要求',
    experience      VARCHAR(20)  DEFAULT NULL COMMENT '经验要求',
    required_skills TEXT         DEFAULT NULL COMMENT '技能要求(JSON数组)',
    description     TEXT         DEFAULT NULL COMMENT '岗位描述',
    job_type        TINYINT      DEFAULT 1 COMMENT '类型: 1-全职 2-实习 3-校招',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    publisher_id    BIGINT       DEFAULT NULL COMMENT '发布人(教师/管理员)',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_job_title (job_title),
    KEY idx_status (status),
    FULLTEXT KEY ft_job (job_title, company, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘岗位表';

-- 人岗匹配记录表
DROP TABLE IF EXISTS employ_job_match;
CREATE TABLE employ_job_match (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    resume_id   BIGINT       NOT NULL COMMENT '关联 employ_resume.id',
    job_id      BIGINT       NOT NULL COMMENT '关联 employ_job.id',
    match_score DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '匹配得分(0-100)',
    match_reason TEXT        DEFAULT NULL COMMENT '匹配理由(AI分析)',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-未查看 1-已投递 2-已面试 3-已录用 4-已拒绝',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resume_job (resume_id, job_id),
    KEY idx_job_match (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人岗匹配记录表';

-- AI模拟面试记录表
DROP TABLE IF EXISTS employ_interview;
CREATE TABLE employ_interview (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL COMMENT '关联 academic_student.id',
    job_id      BIGINT       DEFAULT NULL COMMENT '目标岗位(可选)',
    interview_type TINYINT   NOT NULL DEFAULT 1 COMMENT '类型: 1-文字面试 2-语音面试',
    questions   TEXT         DEFAULT NULL COMMENT '面试问题(JSON)',
    answers     TEXT         DEFAULT NULL COMMENT '回答记录(JSON)',
    score       INT          DEFAULT NULL COMMENT '综合评分',
    feedback    TEXT         DEFAULT NULL COMMENT 'AI反馈与建议',
    duration    INT          DEFAULT NULL COMMENT '面试时长(秒)',
    start_time  DATETIME     DEFAULT NULL,
    end_time    DATETIME     DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_student_interview (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟面试记录表';

-- ============================================================
-- 4. AI 智能体模块
-- ============================================================

-- 知识库文档表
DROP TABLE IF EXISTS ai_knowledge_base;
CREATE TABLE ai_knowledge_base (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL COMMENT '文档标题',
    content     LONGTEXT     DEFAULT NULL COMMENT '原始内容',
    category    VARCHAR(50)  NOT NULL COMMENT '分类: course/job/interview/school_policy',
    tags        VARCHAR(200) DEFAULT NULL COMMENT '标签(逗号分隔)',
    vector_id   VARCHAR(100) DEFAULT NULL COMMENT '向量库ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-未向量化 1-已向量化',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category),
    FULLTEXT KEY ft_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库表';

-- AI对话历史表
DROP TABLE IF EXISTS ai_conversation;
CREATE TABLE ai_conversation (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    session_id  VARCHAR(100) NOT NULL COMMENT '会话ID',
    role        VARCHAR(20)  NOT NULL COMMENT '角色: user/assistant/system',
    content     TEXT         NOT NULL COMMENT '消息内容',
    tokens      INT          DEFAULT 0 COMMENT '消耗Token数',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_session (session_id),
    KEY idx_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- ============================================================
-- 5. 初始化系统数据
-- ============================================================

-- 默认角色
INSERT INTO sys_role (role_name, role_code, remark) VALUES
('超级管理员', 'ROLE_ADMIN', '系统最高权限'),
('教师', 'ROLE_TEACHER', '教师/辅导员权限'),
('学生', 'ROLE_STUDENT', '学生权限');

-- 默认超级管理员 (密码: admin123, BCrypt加密)
-- BCrypt hash for 'admin123'
INSERT INTO sys_user (username, password, real_name, user_type, status) VALUES
('admin', '$2a$10$wsuQZy.Lk9w2jqlIOfSeC.TS3ZvAG0jdNpYMilCc1mhvDWo6LQi62', '系统管理员', 2, 1);

-- 赋权
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 默认菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, sort_order, menu_type, permission, visible) VALUES
(1,  0,  '仪表盘',    '/dashboard',    'dashboard/index',    'DataAnalysis', 1, 1, NULL, 1),
(2,  0,  '学业管理',  '/academic',     NULL,                 'Reading',      2, 1, NULL, 1),
(20, 2,  '学生管理',  '/academic/student', 'academic/student/index', 'User',     1, 1, NULL, 1),
(21, 2,  '学业预警',  '/academic/warning', 'academic/warning/index', 'Warning',  2, 1, NULL, 1),
(22, 2,  '学习规划',  '/academic/plan',    'academic/plan/index',    'Tickets',  3, 1, NULL, 1),
(3,  0,  '就业服务',  '/employment',   NULL,                 'Suitcase',     3, 1, NULL, 1),
(30, 3,  '简历管理',  '/employment/resume',   'employment/resume/index',   'Document',    1, 1, NULL, 1),
(31, 3,  '岗位管理',  '/employment/job',      'employment/job/index',      'Briefcase',   2, 1, NULL, 1),
(32, 3,  '面试记录',  '/employment/interview', 'employment/interview/index', 'ChatDotRound', 3, 1, NULL, 1),
(4,  0,  'AI 智能体', '/ai',           NULL,                 'Cpu',          4, 1, NULL, 1),
(40, 4,  '知识库',    '/ai/knowledge',  'ai/knowledge/index',  'Collection',   1, 1, NULL, 1),
(41, 4,  '对话记录',  '/ai/conversation','ai/conversation/index','ChatLineSquare', 2, 1, NULL, 1),
(5,  0,  '系统管理',  '/system',       NULL,                 'Setting',      5, 1, NULL, 1),
(50, 5,  '用户管理',  '/system/user',    'system/user/index',    'UserFilled',   1, 1, NULL, 1),
(51, 5,  '角色管理',  '/system/role',    'system/role/index',    'Avatar',       2, 1, NULL, 1),
(52, 5,  '菜单管理',  '/system/menu',    'system/menu/index',    'Menu',         3, 1, NULL, 1);
