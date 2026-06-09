-- ============================================================
-- 微信小程序绑定学号功能 — 数据库变更
-- 在 sys_user 表新增 openid 字段
-- ============================================================

ALTER TABLE sys_user
    ADD COLUMN openid VARCHAR(64) NULL COMMENT '微信openid' AFTER avatar,
    ADD UNIQUE INDEX idx_openid (openid);
