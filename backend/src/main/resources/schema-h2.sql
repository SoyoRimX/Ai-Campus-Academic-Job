-- H2 Schema (MySQL 兼容模式)
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(200) NOT NULL,
    real_name   VARCHAR(50)  DEFAULT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    email       VARCHAR(100) DEFAULT NULL,
    avatar      VARCHAR(255) DEFAULT NULL,
    user_type   TINYINT      NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL,
    remark      VARCHAR(200) DEFAULT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    menu_name   VARCHAR(50)  NOT NULL,
    path        VARCHAR(200) DEFAULT NULL,
    component   VARCHAR(200) DEFAULT NULL,
    icon        VARCHAR(50)  DEFAULT NULL,
    sort_order  INT          DEFAULT 0,
    menu_type   TINYINT      NOT NULL DEFAULT 1,
    permission  VARCHAR(100) DEFAULT NULL,
    visible     TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS academic_student (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    student_no       VARCHAR(30)  NOT NULL,
    major            VARCHAR(100) DEFAULT NULL,
    grade            VARCHAR(20)  DEFAULT NULL,
    class_name       VARCHAR(50)  DEFAULT NULL,
    gpa              DECIMAL(4,2) DEFAULT 0.00,
    total_credits    DECIMAL(5,1) DEFAULT 0.0,
    required_credits DECIMAL(5,1) DEFAULT 0.0,
    fail_count       INT          DEFAULT 0,
    advisor          VARCHAR(50)  DEFAULT NULL,
    enrollment_year  INT          DEFAULT NULL,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_student_no (student_no)
);

CREATE TABLE IF NOT EXISTS academic_course (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    course_no   VARCHAR(30)  NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    credit      DECIMAL(3,1) NOT NULL,
    course_type TINYINT      DEFAULT 1,
    semester    VARCHAR(20)  DEFAULT NULL,
    teacher     VARCHAR(50)  DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no)
);

CREATE TABLE IF NOT EXISTS academic_grade (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL,
    course_id   BIGINT       NOT NULL,
    score       DECIMAL(5,2) DEFAULT NULL,
    grade_point DECIMAL(3,2) DEFAULT NULL,
    passed      TINYINT      NOT NULL DEFAULT 1,
    semester    VARCHAR(20)  DEFAULT NULL,
    exam_type   TINYINT      DEFAULT 1,
    remark      VARCHAR(200) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS academic_warning (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    student_id    BIGINT       NOT NULL,
    warning_type  TINYINT      NOT NULL,
    warning_level TINYINT      NOT NULL DEFAULT 1,
    title         VARCHAR(100) NOT NULL,
    description   TEXT         DEFAULT NULL,
    is_read       TINYINT      NOT NULL DEFAULT 0,
    is_handled    TINYINT      NOT NULL DEFAULT 0,
    handle_remark VARCHAR(500) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS academic_study_plan (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    student_id  BIGINT       NOT NULL,
    plan_title  VARCHAR(100) NOT NULL,
    semester    VARCHAR(20)  NOT NULL,
    plan_detail TEXT         DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS employ_resume (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    student_id    BIGINT       NOT NULL,
    title         VARCHAR(100) NOT NULL,
    content       TEXT         DEFAULT NULL,
    target_job    VARCHAR(100) DEFAULT NULL,
    target_city   VARCHAR(50)  DEFAULT NULL,
    ai_score      INT          DEFAULT NULL,
    ai_suggestion TEXT         DEFAULT NULL,
    is_default    TINYINT      NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS employ_job (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    job_title       VARCHAR(100) NOT NULL,
    company         VARCHAR(100) NOT NULL,
    salary_range    VARCHAR(50)  DEFAULT NULL,
    city            VARCHAR(50)  DEFAULT NULL,
    education       VARCHAR(20)  DEFAULT NULL,
    experience      VARCHAR(20)  DEFAULT NULL,
    required_skills TEXT         DEFAULT NULL,
    description     TEXT         DEFAULT NULL,
    job_type        TINYINT      DEFAULT 1,
    status          TINYINT      NOT NULL DEFAULT 1,
    publisher_id    BIGINT       DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS employ_job_match (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    resume_id   BIGINT       NOT NULL,
    job_id      BIGINT       NOT NULL,
    match_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    match_reason TEXT        DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS employ_interview (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    student_id     BIGINT       NOT NULL,
    job_id         BIGINT       DEFAULT NULL,
    interview_type TINYINT      NOT NULL DEFAULT 1,
    questions      TEXT         DEFAULT NULL,
    answers        TEXT         DEFAULT NULL,
    score          INT          DEFAULT NULL,
    feedback       TEXT         DEFAULT NULL,
    duration       INT          DEFAULT NULL,
    start_time     DATETIME     DEFAULT NULL,
    end_time       DATETIME     DEFAULT NULL,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    content     TEXT         DEFAULT NULL,
    category    VARCHAR(50)  NOT NULL,
    tags        VARCHAR(200) DEFAULT NULL,
    vector_id   VARCHAR(100) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_by   BIGINT       DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_conversation (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    session_id  VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    content     TEXT         NOT NULL,
    tokens      INT          DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
