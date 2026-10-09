alter table commodity_group_single
drop column is_delete;

alter table commodity_group_single
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_group_single
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_group_single
    add deleted bit default b'0' null after marking_price;

alter table commodity_group_single
drop column project_id;

alter table commodity_group_single
drop column project_owner_ship;

alter table commodity_activity
    change created_by creator varchar(20) null comment '活动创建人';

alter table commodity_activity
    change updated_by updater varchar(20) null comment '最后修改人';

alter table commodity_activity
    add deleted bit default b'0' null;

alter table commodity_activity
drop column project_id;

alter table commodity_activity
drop column project_owner_ship;


alter table commodity_after_order
drop column is_delete;

alter table commodity_after_order
    add deleted bit default b'0' null after commodity_status;

alter table commodity_after_order
    change create_user_name creator varchar(255) null comment '创建者';

alter table commodity_after_order
    change update_user_name updater varchar(255) null comment '更新者';

alter table commodity_after_order
drop column project_id;

alter table commodity_after_order
drop column project_owner_ship;

alter table commodity_category
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_category
    change update_user_name updater varchar(255) null comment '修改人';

alter table commodity_category
drop column is_delete;

alter table commodity_category
    add deleted bit default b'0' null after dict_value;

alter table commodity_category
drop column project_id;

alter table commodity_category
drop column project_owner_ship;

alter table commodity_setmeal_group
drop column is_delete;

alter table commodity_setmeal_group
    change create_user_name creator varchar(255) null comment '创建者';

alter table commodity_setmeal_group
    change update_user_name updater varchar(255) null comment '更新者';

alter table commodity_setmeal_group
    add deleted bit default b'0' null after commodity_id;

alter table commodity_setmeal_group
drop column project_id;

alter table commodity_setmeal_group
drop column project_owner_ship;

alter table commodity_skus
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_skus
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_skus
drop column is_delete;

alter table commodity_skus
    add deleted bit default b'0' null after is_store;

alter table commodity_skus
drop column project_id;

alter table commodity_skus
drop column project_owner_ship;

alter table commodity_spus
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_spus
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_spus
drop column is_delete;

alter table commodity_spus
drop column project_id;

alter table commodity_spus
drop column project_owner_ship;


alter table commodity_spus
    add deleted bit default b'0' null after dict_value;

alter table commodity_store_category
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_store_category
    change update_user_name updater varchar(255) null comment '上次修改人';

alter table commodity_store_category
    add deleted bit default b'0' null after updater;

alter table commodity_store_category
drop column is_delete;

alter table commodity_store_category
drop column project_id;

alter table commodity_store_category
drop column project_owner_ship;

alter table commodity_store_group
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_store_group
    change update_user_name updater varchar(255) null comment '上次修改人';

alter table commodity_store_group
drop column is_delete;

alter table commodity_store_group
    add deleted bit default b'0' null after commodity_store_group_name;

alter table commodity_store_group
drop column project_id;

alter table commodity_store_group
drop column project_owner_ship;

alter table commodity_store_single
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_store_single
    change update_user_name updater varchar(255) null comment '上次修改人';

alter table commodity_store_single
    add deleted bit default b'0' null after update_time;

alter table commodity_store_single
drop column is_delete;

alter table commodity_store_single
drop column project_id;

alter table commodity_store_single
drop column project_owner_ship;

alter table commodity_store_sku
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_store_sku
    change update_user_name updater varchar(255) null comment '上次修改人';

alter table commodity_store_sku
    add deleted bit default b'0' null after update_time;

alter table commodity_store_sku
drop column is_delete;

alter table commodity_store_sku
drop column project_id;

alter table commodity_store_sku
drop column project_owner_ship;

alter table commodity_store_spu
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_store_spu
    change update_user_name updater varchar(255) null comment '上次修改人';

alter table commodity_store_spu
drop column is_delete;

alter table commodity_store_spu
    add deleted bit default b'0' null after dict_value;

alter table commodity_store_spu
drop column project_id;

alter table commodity_store_spu
drop column project_owner_ship;

alter table commodity_template
drop column is_delete;

alter table commodity_template
    add deleted bit default b'0' null after create_time;

alter table commodity_template
    change create_user_name creator varchar(30) null comment '创建人';

alter table commodity_template
    change update_user_name updater varchar(30) null comment '修改人';

alter table commodity_template
drop column project_id;

alter table commodity_template
drop column project_owner_ship;

alter table commodity_template_category
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_template_category
    change update_user_name updater varchar(255) null comment '修改人';

alter table commodity_template_category
drop column is_delete;

alter table commodity_template_category
    add deleted bit default b'0' null after dict_value;

alter table commodity_template_category
drop column project_id;

alter table commodity_template_category
drop column project_owner_ship;

alter table commodity_template_group_single
drop column is_delete;

alter table commodity_template_group_single
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_template_group_single
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_template_group_single
    add deleted bit default b'0' null after marking_price;

alter table commodity_template_group_single
drop column project_id;

alter table commodity_template_group_single
drop column project_owner_ship;

alter table commodity_template_setmeal_group
    change update_user_name updater varchar(255) null comment '更新者';

alter table commodity_template_setmeal_group
drop column project_id;

alter table commodity_template_setmeal_group
    add deleted bit default b'0' null after commodity_id;

alter table commodity_template_setmeal_group
drop column project_owner_ship;

alter table commodity_template_skus
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_template_skus
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_template_skus
drop column is_delete;

alter table commodity_template_skus
    add deleted bit default b'0' null after is_store;

alter table commodity_template_skus
drop column project_id;

alter table commodity_template_skus
drop column project_owner_ship;

alter table commodity_template_spus
    change create_user_name creator varchar(255) null comment '创建人';

alter table commodity_template_spus
    change update_user_name updater varchar(255) null comment '更新人';

alter table commodity_template_spus
drop column is_delete;

alter table commodity_template_spus
    add deleted bit default b'0' null after dict_value;

alter table commodity_template_spus
drop column project_id;

alter table commodity_template_spus
drop column project_owner_ship;

alter table commodity_activity
    add business_id bigint null;

alter table commodity_after_order
    add business_id bigint null;

alter table commodity_category
    add business_id bigint null;
alter table commodity_group_single
    add business_id bigint null;

alter table commodity_setmeal_group
    add business_id bigint null;
alter table commodity_skus
    add business_id bigint null;
alter table commodity_spus
    add business_id bigint null;

alter table commodity_store_category
    add business_id bigint null;

alter table commodity_store_group
    add business_id bigint null;
alter table commodity_store_single
    add business_id bigint null;
alter table commodity_store_sku
    add business_id bigint null;
alter table commodity_store_spu
    add business_id bigint null;
alter table commodity_template
    add business_id bigint null;
alter table commodity_template_category
    add business_id bigint null;
alter table commodity_template_group_single
    add business_id bigint null;
alter table commodity_template_setmeal_group
    add business_id bigint null;
alter table commodity_template_skus
    add business_id bigint null;
alter table commodity_template_spus
    add business_id bigint null;

CREATE TABLE commodity_tag (
                               id                  int auto_increment comment 'ID',
                               name                VARCHAR(255) not null comment '标签名称',
                               style               VARCHAR(255) not null comment '标签样式',
                               image               VARCHAR(255) not null comment '背景图',
                               creator             varchar(64) collate utf8mb4_unicode_ci default '' null comment '创建者',
                               create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP not null comment '创建时间',
                               updater             varchar(64) collate utf8mb4_unicode_ci default '' null comment '更新者',
                               update_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
                               deleted             bit default b'0' not null comment '是否删除',
                               business_id         bigint null comment '项目 id',
                               PRIMARY KEY (id)
) comment '商品标签表' charset = utf8mb4;


