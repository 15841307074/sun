ALTER TABLE custom_crowd ADD COLUMN `community_flag` int(1) DEFAULT 0 COMMENT '是否在社群 0 没选 1没在  2在群里';
ALTER TABLE good_coupon CHANGE COLUMN store_id  `community_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0不需要进社群 1需要进社群';
ALTER TABLE coupon_package ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0不需要进社群 1需要进社群';
ALTER TABLE activity ADD community_flag int(1) DEFAULT 1 NOT NULL COMMENT '社群专享 1 不开启 2开启';

ALTER TABLE wx_member ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_0 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_1 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_2 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_3 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_4 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_5 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_6 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_7 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_8 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';
ALTER TABLE wx_member_9 ADD COLUMN `community_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1没进社群 2进社群';

ALTER TABLE lottery_settings ADD community_flag int(1) DEFAULT 1 NOT NULL COMMENT '社群专享 1 不开启 2开启';

ALTER TABLE activity_jd ADD community_flag int(1) DEFAULT 1 NOT NULL COMMENT '社群专享 1 不开启 2开启';

ALTER TABLE activity_seckill ADD community_flag int(1) DEFAULT 1 NOT NULL COMMENT '社群专享 1 不开启 2开启';

CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_0 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_1 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_2 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_3 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_4 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_5 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_6 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_7 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_8 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );
CREATE INDEX idx_wx_business_member_login_unionid ON wx_member_9 (
                                                                  business_id,
                                                                  last_login_time,
                                                                  member_id,
                                                                  wx_unionid
    );