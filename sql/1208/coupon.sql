ALTER TABLE good_coupon
    MODIFY COLUMN reduce_amount DECIMAL(10,2) DEFAULT NULL COMMENT 'n件n元/折中的元/折',
    MODIFY COLUMN discount VARCHAR(10) COMMENT 'n件n元/折中的件，也是折扣的折',
    MODIFY COLUMN relief_or_discount DECIMAL(10,2) DEFAULT '0.00' COMMENT '兑换券，满n元可用';