package com.htyoudao.youdao.module.order.dal.dataobject.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BzOrderExampleDO {
    protected String tableFix;

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public String getTableFix() {
        return tableFix;
    }

    public void setTableFix(String tableFix) {
        this.tableFix = tableFix;
    }

    public BzOrderExampleDO() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andOrderIdIsNull() {
            addCriterion("order_id is null");
            return (Criteria) this;
        }

        public Criteria andOrderIdIsNotNull() {
            addCriterion("order_id is not null");
            return (Criteria) this;
        }

        public Criteria andOrderIdEqualTo(Integer value) {
            addCriterion("order_id =", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotEqualTo(Integer value) {
            addCriterion("order_id <>", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdGreaterThan(Integer value) {
            addCriterion("order_id >", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("order_id >=", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdLessThan(Integer value) {
            addCriterion("order_id <", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdLessThanOrEqualTo(Integer value) {
            addCriterion("order_id <=", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdIn(List<Integer> values) {
            addCriterion("order_id in", values, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotIn(List<Integer> values) {
            addCriterion("order_id not in", values, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdBetween(Integer value1, Integer value2) {
            addCriterion("order_id between", value1, value2, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotBetween(Integer value1, Integer value2) {
            addCriterion("order_id not between", value1, value2, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderSnIsNull() {
            addCriterion("order_sn is null");
            return (Criteria) this;
        }

        public Criteria andOrderSnIsNotNull() {
            addCriterion("order_sn is not null");
            return (Criteria) this;
        }

        public Criteria andOrderSnEqualTo(String value) {
            addCriterion("order_sn =", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnNotEqualTo(String value) {
            addCriterion("order_sn <>", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnGreaterThan(String value) {
            addCriterion("order_sn >", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnGreaterThanOrEqualTo(String value) {
            addCriterion("order_sn >=", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnLessThan(String value) {
            addCriterion("order_sn <", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnLessThanOrEqualTo(String value) {
            addCriterion("order_sn <=", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnLike(String value) {
            addCriterion("order_sn like", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnNotLike(String value) {
            addCriterion("order_sn not like", value, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnIn(List<String> values) {
            addCriterion("order_sn in", values, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnNotIn(List<String> values) {
            addCriterion("order_sn not in", values, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnBetween(String value1, String value2) {
            addCriterion("order_sn between", value1, value2, "orderSn");
            return (Criteria) this;
        }

        public Criteria andOrderSnNotBetween(String value1, String value2) {
            addCriterion("order_sn not between", value1, value2, "orderSn");
            return (Criteria) this;
        }

        public Criteria andPaySnIsNull() {
            addCriterion("pay_sn is null");
            return (Criteria) this;
        }

        public Criteria andPaySnIsNotNull() {
            addCriterion("pay_sn is not null");
            return (Criteria) this;
        }

        public Criteria andPaySnEqualTo(String value) {
            addCriterion("pay_sn =", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnNotEqualTo(String value) {
            addCriterion("pay_sn <>", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnGreaterThan(String value) {
            addCriterion("pay_sn >", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnGreaterThanOrEqualTo(String value) {
            addCriterion("pay_sn >=", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnLessThan(String value) {
            addCriterion("pay_sn <", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnLessThanOrEqualTo(String value) {
            addCriterion("pay_sn <=", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnLike(String value) {
            addCriterion("pay_sn like", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnNotLike(String value) {
            addCriterion("pay_sn not like", value, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnIn(List<String> values) {
            addCriterion("pay_sn in", values, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnNotIn(List<String> values) {
            addCriterion("pay_sn not in", values, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnBetween(String value1, String value2) {
            addCriterion("pay_sn between", value1, value2, "paySn");
            return (Criteria) this;
        }

        public Criteria andPaySnNotBetween(String value1, String value2) {
            addCriterion("pay_sn not between", value1, value2, "paySn");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNull() {
            addCriterion("store_id is null");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNotNull() {
            addCriterion("store_id is not null");
            return (Criteria) this;
        }

        public Criteria andStoreIdEqualTo(Long value) {
            addCriterion("store_id =", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotEqualTo(Long value) {
            addCriterion("store_id <>", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThan(Long value) {
            addCriterion("store_id >", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThanOrEqualTo(Long value) {
            addCriterion("store_id >=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThan(Long value) {
            addCriterion("store_id <", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThanOrEqualTo(Long value) {
            addCriterion("store_id <=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdIn(List<Long> values) {
            addCriterion("store_id in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotIn(List<Long> values) {
            addCriterion("store_id not in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdBetween(Long value1, Long value2) {
            addCriterion("store_id between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotBetween(Long value1, Long value2) {
            addCriterion("store_id not between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreNameIsNull() {
            addCriterion("store_name is null");
            return (Criteria) this;
        }

        public Criteria andStoreNameIsNotNull() {
            addCriterion("store_name is not null");
            return (Criteria) this;
        }

        public Criteria andStoreNameEqualTo(String value) {
            addCriterion("store_name =", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameNotEqualTo(String value) {
            addCriterion("store_name <>", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameGreaterThan(String value) {
            addCriterion("store_name >", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameGreaterThanOrEqualTo(String value) {
            addCriterion("store_name >=", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameLessThan(String value) {
            addCriterion("store_name <", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameLessThanOrEqualTo(String value) {
            addCriterion("store_name <=", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameLike(String value) {
            addCriterion("store_name like", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameNotLike(String value) {
            addCriterion("store_name not like", value, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameIn(List<String> values) {
            addCriterion("store_name in", values, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameNotIn(List<String> values) {
            addCriterion("store_name not in", values, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameBetween(String value1, String value2) {
            addCriterion("store_name between", value1, value2, "storeName");
            return (Criteria) this;
        }

        public Criteria andStoreNameNotBetween(String value1, String value2) {
            addCriterion("store_name not between", value1, value2, "storeName");
            return (Criteria) this;
        }

        public Criteria andMemberNameIsNull() {
            addCriterion("member_name is null");
            return (Criteria) this;
        }

        public Criteria andMemberNameIsNotNull() {
            addCriterion("member_name is not null");
            return (Criteria) this;
        }

        public Criteria andMemberNameEqualTo(String value) {
            addCriterion("member_name =", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameNotEqualTo(String value) {
            addCriterion("member_name <>", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameGreaterThan(String value) {
            addCriterion("member_name >", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameGreaterThanOrEqualTo(String value) {
            addCriterion("member_name >=", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameLessThan(String value) {
            addCriterion("member_name <", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameLessThanOrEqualTo(String value) {
            addCriterion("member_name <=", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameLike(String value) {
            addCriterion("member_name like", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameNotLike(String value) {
            addCriterion("member_name not like", value, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameIn(List<String> values) {
            addCriterion("member_name in", values, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameNotIn(List<String> values) {
            addCriterion("member_name not in", values, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameBetween(String value1, String value2) {
            addCriterion("member_name between", value1, value2, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberNameNotBetween(String value1, String value2) {
            addCriterion("member_name not between", value1, value2, "memberName");
            return (Criteria) this;
        }

        public Criteria andMemberIdIsNull() {
            addCriterion("member_id is null");
            return (Criteria) this;
        }

        public Criteria andMemberIdIsNotNull() {
            addCriterion("member_id is not null");
            return (Criteria) this;
        }

        public Criteria andMemberIdEqualTo(Integer value) {
            addCriterion("member_id =", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotEqualTo(Integer value) {
            addCriterion("member_id <>", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdGreaterThan(Integer value) {
            addCriterion("member_id >", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("member_id >=", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdLessThan(Integer value) {
            addCriterion("member_id <", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdLessThanOrEqualTo(Integer value) {
            addCriterion("member_id <=", value, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdIn(List<Integer> values) {
            addCriterion("member_id in", values, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotIn(List<Integer> values) {
            addCriterion("member_id not in", values, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdBetween(Integer value1, Integer value2) {
            addCriterion("member_id between", value1, value2, "memberId");
            return (Criteria) this;
        }

        public Criteria andMemberIdNotBetween(Integer value1, Integer value2) {
            addCriterion("member_id not between", value1, value2, "memberId");
            return (Criteria) this;
        }

        public Criteria andPayTimeIsNull() {
            addCriterion("pay_time is null");
            return (Criteria) this;
        }

        public Criteria andPayTimeIsNotNull() {
            addCriterion("pay_time is not null");
            return (Criteria) this;
        }

        public Criteria andPayTimeEqualTo(Date value) {
            addCriterion("pay_time =", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeNotEqualTo(Date value) {
            addCriterion("pay_time <>", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeGreaterThan(Date value) {
            addCriterion("pay_time >", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("pay_time >=", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeLessThan(Date value) {
            addCriterion("pay_time <", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeLessThanOrEqualTo(Date value) {
            addCriterion("pay_time <=", value, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeIn(List<Date> values) {
            addCriterion("pay_time in", values, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeNotIn(List<Date> values) {
            addCriterion("pay_time not in", values, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeBetween(Date value1, Date value2) {
            addCriterion("pay_time between", value1, value2, "payTime");
            return (Criteria) this;
        }

        public Criteria andPayTimeNotBetween(Date value1, Date value2) {
            addCriterion("pay_time not between", value1, value2, "payTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeIsNull() {
            addCriterion("finish_time is null");
            return (Criteria) this;
        }

        public Criteria andFinishTimeIsNotNull() {
            addCriterion("finish_time is not null");
            return (Criteria) this;
        }

        public Criteria andFinishTimeEqualTo(Date value) {
            addCriterion("finish_time =", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotEqualTo(Date value) {
            addCriterion("finish_time <>", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeGreaterThan(Date value) {
            addCriterion("finish_time >", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("finish_time >=", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeLessThan(Date value) {
            addCriterion("finish_time <", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeLessThanOrEqualTo(Date value) {
            addCriterion("finish_time <=", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeIn(List<Date> values) {
            addCriterion("finish_time in", values, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotIn(List<Date> values) {
            addCriterion("finish_time not in", values, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeBetween(Date value1, Date value2) {
            addCriterion("finish_time between", value1, value2, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotBetween(Date value1, Date value2) {
            addCriterion("finish_time not between", value1, value2, "finishTime");
            return (Criteria) this;
        }

        public Criteria andOrderStateIsNull() {
            addCriterion("order_state is null");
            return (Criteria) this;
        }

        public Criteria andOrderStateIsNotNull() {
            addCriterion("order_state is not null");
            return (Criteria) this;
        }

        public Criteria andOrderStateEqualTo(Integer value) {
            addCriterion("order_state =", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateNotEqualTo(Integer value) {
            addCriterion("order_state <>", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateGreaterThan(Integer value) {
            addCriterion("order_state >", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateGreaterThanOrEqualTo(Integer value) {
            addCriterion("order_state >=", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateLessThan(Integer value) {
            addCriterion("order_state <", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateLessThanOrEqualTo(Integer value) {
            addCriterion("order_state <=", value, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateIn(List<Integer> values) {
            addCriterion("order_state in", values, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateNotIn(List<Integer> values) {
            addCriterion("order_state not in", values, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateBetween(Integer value1, Integer value2) {
            addCriterion("order_state between", value1, value2, "orderState");
            return (Criteria) this;
        }

        public Criteria andOrderStateNotBetween(Integer value1, Integer value2) {
            addCriterion("order_state not between", value1, value2, "orderState");
            return (Criteria) this;
        }

        public Criteria andPaymentNameIsNull() {
            addCriterion("payment_name is null");
            return (Criteria) this;
        }

        public Criteria andPaymentNameIsNotNull() {
            addCriterion("payment_name is not null");
            return (Criteria) this;
        }

        public Criteria andPaymentNameEqualTo(String value) {
            addCriterion("payment_name =", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameNotEqualTo(String value) {
            addCriterion("payment_name <>", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameGreaterThan(String value) {
            addCriterion("payment_name >", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameGreaterThanOrEqualTo(String value) {
            addCriterion("payment_name >=", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameLessThan(String value) {
            addCriterion("payment_name <", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameLessThanOrEqualTo(String value) {
            addCriterion("payment_name <=", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameLike(String value) {
            addCriterion("payment_name like", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameNotLike(String value) {
            addCriterion("payment_name not like", value, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameIn(List<String> values) {
            addCriterion("payment_name in", values, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameNotIn(List<String> values) {
            addCriterion("payment_name not in", values, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameBetween(String value1, String value2) {
            addCriterion("payment_name between", value1, value2, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentNameNotBetween(String value1, String value2) {
            addCriterion("payment_name not between", value1, value2, "paymentName");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeIsNull() {
            addCriterion("payment_code is null");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeIsNotNull() {
            addCriterion("payment_code is not null");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeEqualTo(String value) {
            addCriterion("payment_code =", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeNotEqualTo(String value) {
            addCriterion("payment_code <>", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeGreaterThan(String value) {
            addCriterion("payment_code >", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeGreaterThanOrEqualTo(String value) {
            addCriterion("payment_code >=", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeLessThan(String value) {
            addCriterion("payment_code <", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeLessThanOrEqualTo(String value) {
            addCriterion("payment_code <=", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeLike(String value) {
            addCriterion("payment_code like", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeNotLike(String value) {
            addCriterion("payment_code not like", value, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeIn(List<String> values) {
            addCriterion("payment_code in", values, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeNotIn(List<String> values) {
            addCriterion("payment_code not in", values, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeBetween(String value1, String value2) {
            addCriterion("payment_code between", value1, value2, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andPaymentCodeNotBetween(String value1, String value2) {
            addCriterion("payment_code not between", value1, value2, "paymentCode");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountIsNull() {
            addCriterion("goods_amount is null");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountIsNotNull() {
            addCriterion("goods_amount is not null");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountEqualTo(BigDecimal value) {
            addCriterion("goods_amount =", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountNotEqualTo(BigDecimal value) {
            addCriterion("goods_amount <>", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountGreaterThan(BigDecimal value) {
            addCriterion("goods_amount >", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("goods_amount >=", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountLessThan(BigDecimal value) {
            addCriterion("goods_amount <", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("goods_amount <=", value, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountIn(List<BigDecimal> values) {
            addCriterion("goods_amount in", values, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountNotIn(List<BigDecimal> values) {
            addCriterion("goods_amount not in", values, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("goods_amount between", value1, value2, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andGoodsAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("goods_amount not between", value1, value2, "goodsAmount");
            return (Criteria) this;
        }

        public Criteria andExpressFeeIsNull() {
            addCriterion("express_fee is null");
            return (Criteria) this;
        }

        public Criteria andExpressFeeIsNotNull() {
            addCriterion("express_fee is not null");
            return (Criteria) this;
        }

        public Criteria andExpressFeeEqualTo(BigDecimal value) {
            addCriterion("express_fee =", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeNotEqualTo(BigDecimal value) {
            addCriterion("express_fee <>", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeGreaterThan(BigDecimal value) {
            addCriterion("express_fee >", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("express_fee >=", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeLessThan(BigDecimal value) {
            addCriterion("express_fee <", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("express_fee <=", value, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeIn(List<BigDecimal> values) {
            addCriterion("express_fee in", values, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeNotIn(List<BigDecimal> values) {
            addCriterion("express_fee not in", values, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("express_fee between", value1, value2, "expressFee");
            return (Criteria) this;
        }

        public Criteria andExpressFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("express_fee not between", value1, value2, "expressFee");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountIsNull() {
            addCriterion("activity_discount_amount is null");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountIsNotNull() {
            addCriterion("activity_discount_amount is not null");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountEqualTo(BigDecimal value) {
            addCriterion("activity_discount_amount =", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountNotEqualTo(BigDecimal value) {
            addCriterion("activity_discount_amount <>", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountGreaterThan(BigDecimal value) {
            addCriterion("activity_discount_amount >", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("activity_discount_amount >=", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountLessThan(BigDecimal value) {
            addCriterion("activity_discount_amount <", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("activity_discount_amount <=", value, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountIn(List<BigDecimal> values) {
            addCriterion("activity_discount_amount in", values, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountNotIn(List<BigDecimal> values) {
            addCriterion("activity_discount_amount not in", values, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("activity_discount_amount between", value1, value2, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andActivityDiscountAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("activity_discount_amount not between", value1, value2, "activityDiscountAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIsNull() {
            addCriterion("order_amount is null");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIsNotNull() {
            addCriterion("order_amount is not null");
            return (Criteria) this;
        }

        public Criteria andOrderAmountEqualTo(BigDecimal value) {
            addCriterion("order_amount =", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotEqualTo(BigDecimal value) {
            addCriterion("order_amount <>", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountGreaterThan(BigDecimal value) {
            addCriterion("order_amount >", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("order_amount >=", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountLessThan(BigDecimal value) {
            addCriterion("order_amount <", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("order_amount <=", value, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountIn(List<BigDecimal> values) {
            addCriterion("order_amount in", values, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotIn(List<BigDecimal> values) {
            addCriterion("order_amount not in", values, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_amount between", value1, value2, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andOrderAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_amount not between", value1, value2, "orderAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountIsNull() {
            addCriterion("balance_amount is null");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountIsNotNull() {
            addCriterion("balance_amount is not null");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountEqualTo(BigDecimal value) {
            addCriterion("balance_amount =", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountNotEqualTo(BigDecimal value) {
            addCriterion("balance_amount <>", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountGreaterThan(BigDecimal value) {
            addCriterion("balance_amount >", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("balance_amount >=", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountLessThan(BigDecimal value) {
            addCriterion("balance_amount <", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("balance_amount <=", value, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountIn(List<BigDecimal> values) {
            addCriterion("balance_amount in", values, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountNotIn(List<BigDecimal> values) {
            addCriterion("balance_amount not in", values, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("balance_amount between", value1, value2, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andBalanceAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("balance_amount not between", value1, value2, "balanceAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountIsNull() {
            addCriterion("pay_amount is null");
            return (Criteria) this;
        }

        public Criteria andPayAmountIsNotNull() {
            addCriterion("pay_amount is not null");
            return (Criteria) this;
        }

        public Criteria andPayAmountEqualTo(BigDecimal value) {
            addCriterion("pay_amount =", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountNotEqualTo(BigDecimal value) {
            addCriterion("pay_amount <>", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountGreaterThan(BigDecimal value) {
            addCriterion("pay_amount >", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pay_amount >=", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountLessThan(BigDecimal value) {
            addCriterion("pay_amount <", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pay_amount <=", value, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountIn(List<BigDecimal> values) {
            addCriterion("pay_amount in", values, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountNotIn(List<BigDecimal> values) {
            addCriterion("pay_amount not in", values, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay_amount between", value1, value2, "payAmount");
            return (Criteria) this;
        }

        public Criteria andPayAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay_amount not between", value1, value2, "payAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountIsNull() {
            addCriterion("refund_amount is null");
            return (Criteria) this;
        }

        public Criteria andRefundAmountIsNotNull() {
            addCriterion("refund_amount is not null");
            return (Criteria) this;
        }

        public Criteria andRefundAmountEqualTo(BigDecimal value) {
            addCriterion("refund_amount =", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountNotEqualTo(BigDecimal value) {
            addCriterion("refund_amount <>", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountGreaterThan(BigDecimal value) {
            addCriterion("refund_amount >", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("refund_amount >=", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountLessThan(BigDecimal value) {
            addCriterion("refund_amount <", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("refund_amount <=", value, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountIn(List<BigDecimal> values) {
            addCriterion("refund_amount in", values, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountNotIn(List<BigDecimal> values) {
            addCriterion("refund_amount not in", values, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("refund_amount between", value1, value2, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andRefundAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("refund_amount not between", value1, value2, "refundAmount");
            return (Criteria) this;
        }

        public Criteria andReceiverNameIsNull() {
            addCriterion("receiver_name is null");
            return (Criteria) this;
        }

        public Criteria andReceiverNameIsNotNull() {
            addCriterion("receiver_name is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverNameEqualTo(String value) {
            addCriterion("receiver_name =", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameNotEqualTo(String value) {
            addCriterion("receiver_name <>", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameGreaterThan(String value) {
            addCriterion("receiver_name >", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_name >=", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameLessThan(String value) {
            addCriterion("receiver_name <", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameLessThanOrEqualTo(String value) {
            addCriterion("receiver_name <=", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameLike(String value) {
            addCriterion("receiver_name like", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameNotLike(String value) {
            addCriterion("receiver_name not like", value, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameIn(List<String> values) {
            addCriterion("receiver_name in", values, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameNotIn(List<String> values) {
            addCriterion("receiver_name not in", values, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameBetween(String value1, String value2) {
            addCriterion("receiver_name between", value1, value2, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverNameNotBetween(String value1, String value2) {
            addCriterion("receiver_name not between", value1, value2, "receiverName");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoIsNull() {
            addCriterion("receiver_area_info is null");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoIsNotNull() {
            addCriterion("receiver_area_info is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoEqualTo(String value) {
            addCriterion("receiver_area_info =", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoNotEqualTo(String value) {
            addCriterion("receiver_area_info <>", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoGreaterThan(String value) {
            addCriterion("receiver_area_info >", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_area_info >=", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoLessThan(String value) {
            addCriterion("receiver_area_info <", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoLessThanOrEqualTo(String value) {
            addCriterion("receiver_area_info <=", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoLike(String value) {
            addCriterion("receiver_area_info like", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoNotLike(String value) {
            addCriterion("receiver_area_info not like", value, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoIn(List<String> values) {
            addCriterion("receiver_area_info in", values, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoNotIn(List<String> values) {
            addCriterion("receiver_area_info not in", values, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoBetween(String value1, String value2) {
            addCriterion("receiver_area_info between", value1, value2, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAreaInfoNotBetween(String value1, String value2) {
            addCriterion("receiver_area_info not between", value1, value2, "receiverAreaInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressIsNull() {
            addCriterion("receiver_address is null");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressIsNotNull() {
            addCriterion("receiver_address is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressEqualTo(String value) {
            addCriterion("receiver_address =", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressNotEqualTo(String value) {
            addCriterion("receiver_address <>", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressGreaterThan(String value) {
            addCriterion("receiver_address >", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_address >=", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressLessThan(String value) {
            addCriterion("receiver_address <", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressLessThanOrEqualTo(String value) {
            addCriterion("receiver_address <=", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressLike(String value) {
            addCriterion("receiver_address like", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressNotLike(String value) {
            addCriterion("receiver_address not like", value, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressIn(List<String> values) {
            addCriterion("receiver_address in", values, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressNotIn(List<String> values) {
            addCriterion("receiver_address not in", values, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressBetween(String value1, String value2) {
            addCriterion("receiver_address between", value1, value2, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverAddressNotBetween(String value1, String value2) {
            addCriterion("receiver_address not between", value1, value2, "receiverAddress");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileIsNull() {
            addCriterion("receiver_mobile is null");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileIsNotNull() {
            addCriterion("receiver_mobile is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileEqualTo(String value) {
            addCriterion("receiver_mobile =", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileNotEqualTo(String value) {
            addCriterion("receiver_mobile <>", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileGreaterThan(String value) {
            addCriterion("receiver_mobile >", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_mobile >=", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileLessThan(String value) {
            addCriterion("receiver_mobile <", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileLessThanOrEqualTo(String value) {
            addCriterion("receiver_mobile <=", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileLike(String value) {
            addCriterion("receiver_mobile like", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileNotLike(String value) {
            addCriterion("receiver_mobile not like", value, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileIn(List<String> values) {
            addCriterion("receiver_mobile in", values, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileNotIn(List<String> values) {
            addCriterion("receiver_mobile not in", values, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileBetween(String value1, String value2) {
            addCriterion("receiver_mobile between", value1, value2, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andReceiverMobileNotBetween(String value1, String value2) {
            addCriterion("receiver_mobile not between", value1, value2, "receiverMobile");
            return (Criteria) this;
        }

        public Criteria andDelayDaysIsNull() {
            addCriterion("delay_days is null");
            return (Criteria) this;
        }

        public Criteria andDelayDaysIsNotNull() {
            addCriterion("delay_days is not null");
            return (Criteria) this;
        }

        public Criteria andDelayDaysEqualTo(Integer value) {
            addCriterion("delay_days =", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysNotEqualTo(Integer value) {
            addCriterion("delay_days <>", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysGreaterThan(Integer value) {
            addCriterion("delay_days >", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysGreaterThanOrEqualTo(Integer value) {
            addCriterion("delay_days >=", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysLessThan(Integer value) {
            addCriterion("delay_days <", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysLessThanOrEqualTo(Integer value) {
            addCriterion("delay_days <=", value, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysIn(List<Integer> values) {
            addCriterion("delay_days in", values, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysNotIn(List<Integer> values) {
            addCriterion("delay_days not in", values, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysBetween(Integer value1, Integer value2) {
            addCriterion("delay_days between", value1, value2, "delayDays");
            return (Criteria) this;
        }

        public Criteria andDelayDaysNotBetween(Integer value1, Integer value2) {
            addCriterion("delay_days not between", value1, value2, "delayDays");
            return (Criteria) this;
        }

        public Criteria andExpressIdIsNull() {
            addCriterion("express_id is null");
            return (Criteria) this;
        }

        public Criteria andExpressIdIsNotNull() {
            addCriterion("express_id is not null");
            return (Criteria) this;
        }

        public Criteria andExpressIdEqualTo(Integer value) {
            addCriterion("express_id =", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdNotEqualTo(Integer value) {
            addCriterion("express_id <>", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdGreaterThan(Integer value) {
            addCriterion("express_id >", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("express_id >=", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdLessThan(Integer value) {
            addCriterion("express_id <", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdLessThanOrEqualTo(Integer value) {
            addCriterion("express_id <=", value, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdIn(List<Integer> values) {
            addCriterion("express_id in", values, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdNotIn(List<Integer> values) {
            addCriterion("express_id not in", values, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdBetween(Integer value1, Integer value2) {
            addCriterion("express_id between", value1, value2, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressIdNotBetween(Integer value1, Integer value2) {
            addCriterion("express_id not between", value1, value2, "expressId");
            return (Criteria) this;
        }

        public Criteria andExpressCodeIsNull() {
            addCriterion("express_code is null");
            return (Criteria) this;
        }

        public Criteria andExpressCodeIsNotNull() {
            addCriterion("express_code is not null");
            return (Criteria) this;
        }

        public Criteria andExpressCodeEqualTo(String value) {
            addCriterion("express_code =", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeNotEqualTo(String value) {
            addCriterion("express_code <>", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeGreaterThan(String value) {
            addCriterion("express_code >", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeGreaterThanOrEqualTo(String value) {
            addCriterion("express_code >=", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeLessThan(String value) {
            addCriterion("express_code <", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeLessThanOrEqualTo(String value) {
            addCriterion("express_code <=", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeLike(String value) {
            addCriterion("express_code like", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeNotLike(String value) {
            addCriterion("express_code not like", value, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeIn(List<String> values) {
            addCriterion("express_code in", values, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeNotIn(List<String> values) {
            addCriterion("express_code not in", values, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeBetween(String value1, String value2) {
            addCriterion("express_code between", value1, value2, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressCodeNotBetween(String value1, String value2) {
            addCriterion("express_code not between", value1, value2, "expressCode");
            return (Criteria) this;
        }

        public Criteria andExpressNameIsNull() {
            addCriterion("express_name is null");
            return (Criteria) this;
        }

        public Criteria andExpressNameIsNotNull() {
            addCriterion("express_name is not null");
            return (Criteria) this;
        }

        public Criteria andExpressNameEqualTo(String value) {
            addCriterion("express_name =", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameNotEqualTo(String value) {
            addCriterion("express_name <>", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameGreaterThan(String value) {
            addCriterion("express_name >", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameGreaterThanOrEqualTo(String value) {
            addCriterion("express_name >=", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameLessThan(String value) {
            addCriterion("express_name <", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameLessThanOrEqualTo(String value) {
            addCriterion("express_name <=", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameLike(String value) {
            addCriterion("express_name like", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameNotLike(String value) {
            addCriterion("express_name not like", value, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameIn(List<String> values) {
            addCriterion("express_name in", values, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameNotIn(List<String> values) {
            addCriterion("express_name not in", values, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameBetween(String value1, String value2) {
            addCriterion("express_name between", value1, value2, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNameNotBetween(String value1, String value2) {
            addCriterion("express_name not between", value1, value2, "expressName");
            return (Criteria) this;
        }

        public Criteria andExpressNumberIsNull() {
            addCriterion("express_number is null");
            return (Criteria) this;
        }

        public Criteria andExpressNumberIsNotNull() {
            addCriterion("express_number is not null");
            return (Criteria) this;
        }

        public Criteria andExpressNumberEqualTo(String value) {
            addCriterion("express_number =", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberNotEqualTo(String value) {
            addCriterion("express_number <>", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberGreaterThan(String value) {
            addCriterion("express_number >", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberGreaterThanOrEqualTo(String value) {
            addCriterion("express_number >=", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberLessThan(String value) {
            addCriterion("express_number <", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberLessThanOrEqualTo(String value) {
            addCriterion("express_number <=", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberLike(String value) {
            addCriterion("express_number like", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberNotLike(String value) {
            addCriterion("express_number not like", value, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberIn(List<String> values) {
            addCriterion("express_number in", values, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberNotIn(List<String> values) {
            addCriterion("express_number not in", values, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberBetween(String value1, String value2) {
            addCriterion("express_number between", value1, value2, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andExpressNumberNotBetween(String value1, String value2) {
            addCriterion("express_number not between", value1, value2, "expressNumber");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateIsNull() {
            addCriterion("evaluate_state is null");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateIsNotNull() {
            addCriterion("evaluate_state is not null");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateEqualTo(Boolean value) {
            addCriterion("evaluate_state =", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateNotEqualTo(Boolean value) {
            addCriterion("evaluate_state <>", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateGreaterThan(Boolean value) {
            addCriterion("evaluate_state >", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateGreaterThanOrEqualTo(Boolean value) {
            addCriterion("evaluate_state >=", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateLessThan(Boolean value) {
            addCriterion("evaluate_state <", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateLessThanOrEqualTo(Boolean value) {
            addCriterion("evaluate_state <=", value, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateIn(List<Boolean> values) {
            addCriterion("evaluate_state in", values, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateNotIn(List<Boolean> values) {
            addCriterion("evaluate_state not in", values, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateBetween(Boolean value1, Boolean value2) {
            addCriterion("evaluate_state between", value1, value2, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andEvaluateStateNotBetween(Boolean value1, Boolean value2) {
            addCriterion("evaluate_state not between", value1, value2, "evaluateState");
            return (Criteria) this;
        }

        public Criteria andOrderTypeIsNull() {
            addCriterion("order_type is null");
            return (Criteria) this;
        }

        public Criteria andOrderTypeIsNotNull() {
            addCriterion("order_type is not null");
            return (Criteria) this;
        }

        public Criteria andOrderTypeEqualTo(Byte value) {
            addCriterion("order_type =", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeNotEqualTo(Byte value) {
            addCriterion("order_type <>", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeGreaterThan(Byte value) {
            addCriterion("order_type >", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("order_type >=", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeLessThan(Byte value) {
            addCriterion("order_type <", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeLessThanOrEqualTo(Byte value) {
            addCriterion("order_type <=", value, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeIn(List<Byte> values) {
            addCriterion("order_type in", values, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeNotIn(List<Byte> values) {
            addCriterion("order_type not in", values, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeBetween(Byte value1, Byte value2) {
            addCriterion("order_type between", value1, value2, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("order_type not between", value1, value2, "orderType");
            return (Criteria) this;
        }

        public Criteria andOrderFromIsNull() {
            addCriterion("order_from is null");
            return (Criteria) this;
        }

        public Criteria andOrderFromIsNotNull() {
            addCriterion("order_from is not null");
            return (Criteria) this;
        }

        public Criteria andOrderFromEqualTo(Byte value) {
            addCriterion("order_from =", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromNotEqualTo(Byte value) {
            addCriterion("order_from <>", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromGreaterThan(Byte value) {
            addCriterion("order_from >", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromGreaterThanOrEqualTo(Byte value) {
            addCriterion("order_from >=", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromLessThan(Byte value) {
            addCriterion("order_from <", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromLessThanOrEqualTo(Byte value) {
            addCriterion("order_from <=", value, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromIn(List<Byte> values) {
            addCriterion("order_from in", values, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromNotIn(List<Byte> values) {
            addCriterion("order_from not in", values, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromBetween(Byte value1, Byte value2) {
            addCriterion("order_from between", value1, value2, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andOrderFromNotBetween(Byte value1, Byte value2) {
            addCriterion("order_from not between", value1, value2, "orderFrom");
            return (Criteria) this;
        }

        public Criteria andLockStateIsNull() {
            addCriterion("lock_state is null");
            return (Criteria) this;
        }

        public Criteria andLockStateIsNotNull() {
            addCriterion("lock_state is not null");
            return (Criteria) this;
        }

        public Criteria andLockStateEqualTo(Byte value) {
            addCriterion("lock_state =", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateNotEqualTo(Byte value) {
            addCriterion("lock_state <>", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateGreaterThan(Byte value) {
            addCriterion("lock_state >", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateGreaterThanOrEqualTo(Byte value) {
            addCriterion("lock_state >=", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateLessThan(Byte value) {
            addCriterion("lock_state <", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateLessThanOrEqualTo(Byte value) {
            addCriterion("lock_state <=", value, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateIn(List<Byte> values) {
            addCriterion("lock_state in", values, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateNotIn(List<Byte> values) {
            addCriterion("lock_state not in", values, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateBetween(Byte value1, Byte value2) {
            addCriterion("lock_state between", value1, value2, "lockState");
            return (Criteria) this;
        }

        public Criteria andLockStateNotBetween(Byte value1, Byte value2) {
            addCriterion("lock_state not between", value1, value2, "lockState");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonIsNull() {
            addCriterion("refuse_reason is null");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonIsNotNull() {
            addCriterion("refuse_reason is not null");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonEqualTo(String value) {
            addCriterion("refuse_reason =", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonNotEqualTo(String value) {
            addCriterion("refuse_reason <>", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonGreaterThan(String value) {
            addCriterion("refuse_reason >", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonGreaterThanOrEqualTo(String value) {
            addCriterion("refuse_reason >=", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonLessThan(String value) {
            addCriterion("refuse_reason <", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonLessThanOrEqualTo(String value) {
            addCriterion("refuse_reason <=", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonLike(String value) {
            addCriterion("refuse_reason like", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonNotLike(String value) {
            addCriterion("refuse_reason not like", value, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonIn(List<String> values) {
            addCriterion("refuse_reason in", values, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonNotIn(List<String> values) {
            addCriterion("refuse_reason not in", values, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonBetween(String value1, String value2) {
            addCriterion("refuse_reason between", value1, value2, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseReasonNotBetween(String value1, String value2) {
            addCriterion("refuse_reason not between", value1, value2, "refuseReason");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkIsNull() {
            addCriterion("refuse_remark is null");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkIsNotNull() {
            addCriterion("refuse_remark is not null");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkEqualTo(String value) {
            addCriterion("refuse_remark =", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkNotEqualTo(String value) {
            addCriterion("refuse_remark <>", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkGreaterThan(String value) {
            addCriterion("refuse_remark >", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("refuse_remark >=", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkLessThan(String value) {
            addCriterion("refuse_remark <", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkLessThanOrEqualTo(String value) {
            addCriterion("refuse_remark <=", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkLike(String value) {
            addCriterion("refuse_remark like", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkNotLike(String value) {
            addCriterion("refuse_remark not like", value, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkIn(List<String> values) {
            addCriterion("refuse_remark in", values, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkNotIn(List<String> values) {
            addCriterion("refuse_remark not in", values, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkBetween(String value1, String value2) {
            addCriterion("refuse_remark between", value1, value2, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andRefuseRemarkNotBetween(String value1, String value2) {
            addCriterion("refuse_remark not between", value1, value2, "refuseRemark");
            return (Criteria) this;
        }

        public Criteria andIsSettlementIsNull() {
            addCriterion("is_settlement is null");
            return (Criteria) this;
        }

        public Criteria andIsSettlementIsNotNull() {
            addCriterion("is_settlement is not null");
            return (Criteria) this;
        }

        public Criteria andIsSettlementEqualTo(Byte value) {
            addCriterion("is_settlement =", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementNotEqualTo(Byte value) {
            addCriterion("is_settlement <>", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementGreaterThan(Byte value) {
            addCriterion("is_settlement >", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_settlement >=", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementLessThan(Byte value) {
            addCriterion("is_settlement <", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementLessThanOrEqualTo(Byte value) {
            addCriterion("is_settlement <=", value, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementIn(List<Byte> values) {
            addCriterion("is_settlement in", values, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementNotIn(List<Byte> values) {
            addCriterion("is_settlement not in", values, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementBetween(Byte value1, Byte value2) {
            addCriterion("is_settlement between", value1, value2, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsSettlementNotBetween(Byte value1, Byte value2) {
            addCriterion("is_settlement not between", value1, value2, "isSettlement");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetIsNull() {
            addCriterion("is_generate_facesheet is null");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetIsNotNull() {
            addCriterion("is_generate_facesheet is not null");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetEqualTo(Byte value) {
            addCriterion("is_generate_facesheet =", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetNotEqualTo(Byte value) {
            addCriterion("is_generate_facesheet <>", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetGreaterThan(Byte value) {
            addCriterion("is_generate_facesheet >", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_generate_facesheet >=", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetLessThan(Byte value) {
            addCriterion("is_generate_facesheet <", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetLessThanOrEqualTo(Byte value) {
            addCriterion("is_generate_facesheet <=", value, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetIn(List<Byte> values) {
            addCriterion("is_generate_facesheet in", values, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetNotIn(List<Byte> values) {
            addCriterion("is_generate_facesheet not in", values, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetBetween(Byte value1, Byte value2) {
            addCriterion("is_generate_facesheet between", value1, value2, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsGenerateFacesheetNotBetween(Byte value1, Byte value2) {
            addCriterion("is_generate_facesheet not between", value1, value2, "isGenerateFacesheet");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsIsNull() {
            addCriterion("is_virtual_goods is null");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsIsNotNull() {
            addCriterion("is_virtual_goods is not null");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsEqualTo(Byte value) {
            addCriterion("is_virtual_goods =", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsNotEqualTo(Byte value) {
            addCriterion("is_virtual_goods <>", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsGreaterThan(Byte value) {
            addCriterion("is_virtual_goods >", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_virtual_goods >=", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsLessThan(Byte value) {
            addCriterion("is_virtual_goods <", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsLessThanOrEqualTo(Byte value) {
            addCriterion("is_virtual_goods <=", value, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsIn(List<Byte> values) {
            addCriterion("is_virtual_goods in", values, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsNotIn(List<Byte> values) {
            addCriterion("is_virtual_goods not in", values, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsBetween(Byte value1, Byte value2) {
            addCriterion("is_virtual_goods between", value1, value2, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andIsVirtualGoodsNotBetween(Byte value1, Byte value2) {
            addCriterion("is_virtual_goods not between", value1, value2, "isVirtualGoods");
            return (Criteria) this;
        }

        public Criteria andStarIsNull() {
            addCriterion("star is null");
            return (Criteria) this;
        }

        public Criteria andStarIsNotNull() {
            addCriterion("star is not null");
            return (Criteria) this;
        }

        public Criteria andStarEqualTo(Integer value) {
            addCriterion("star =", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarNotEqualTo(Integer value) {
            addCriterion("star <>", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarGreaterThan(Integer value) {
            addCriterion("star >", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarGreaterThanOrEqualTo(Integer value) {
            addCriterion("star >=", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarLessThan(Integer value) {
            addCriterion("star <", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarLessThanOrEqualTo(Integer value) {
            addCriterion("star <=", value, "star");
            return (Criteria) this;
        }

        public Criteria andStarIn(List<Integer> values) {
            addCriterion("star in", values, "star");
            return (Criteria) this;
        }

        public Criteria andStarNotIn(List<Integer> values) {
            addCriterion("star not in", values, "star");
            return (Criteria) this;
        }

        public Criteria andStarBetween(Integer value1, Integer value2) {
            addCriterion("star between", value1, value2, "star");
            return (Criteria) this;
        }

        public Criteria andStarNotBetween(Integer value1, Integer value2) {
            addCriterion("star not between", value1, value2, "star");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkIsNull() {
            addCriterion("store_remark is null");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkIsNotNull() {
            addCriterion("store_remark is not null");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkEqualTo(String value) {
            addCriterion("store_remark =", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkNotEqualTo(String value) {
            addCriterion("store_remark <>", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkGreaterThan(String value) {
            addCriterion("store_remark >", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("store_remark >=", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkLessThan(String value) {
            addCriterion("store_remark <", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkLessThanOrEqualTo(String value) {
            addCriterion("store_remark <=", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkLike(String value) {
            addCriterion("store_remark like", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkNotLike(String value) {
            addCriterion("store_remark not like", value, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkIn(List<String> values) {
            addCriterion("store_remark in", values, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkNotIn(List<String> values) {
            addCriterion("store_remark not in", values, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkBetween(String value1, String value2) {
            addCriterion("store_remark between", value1, value2, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andStoreRemarkNotBetween(String value1, String value2) {
            addCriterion("store_remark not between", value1, value2, "storeRemark");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameIsNull() {
            addCriterion("take_away_name is null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameIsNotNull() {
            addCriterion("take_away_name is not null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameEqualTo(String value) {
            addCriterion("take_away_name =", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameNotEqualTo(String value) {
            addCriterion("take_away_name <>", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameGreaterThan(String value) {
            addCriterion("take_away_name >", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameGreaterThanOrEqualTo(String value) {
            addCriterion("take_away_name >=", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameLessThan(String value) {
            addCriterion("take_away_name <", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameLessThanOrEqualTo(String value) {
            addCriterion("take_away_name <=", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameLike(String value) {
            addCriterion("take_away_name like", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameNotLike(String value) {
            addCriterion("take_away_name not like", value, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameIn(List<String> values) {
            addCriterion("take_away_name in", values, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameNotIn(List<String> values) {
            addCriterion("take_away_name not in", values, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameBetween(String value1, String value2) {
            addCriterion("take_away_name between", value1, value2, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayNameNotBetween(String value1, String value2) {
            addCriterion("take_away_name not between", value1, value2, "takeAwayName");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelIsNull() {
            addCriterion("take_away_tel is null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelIsNotNull() {
            addCriterion("take_away_tel is not null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelEqualTo(String value) {
            addCriterion("take_away_tel =", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelNotEqualTo(String value) {
            addCriterion("take_away_tel <>", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelGreaterThan(String value) {
            addCriterion("take_away_tel >", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelGreaterThanOrEqualTo(String value) {
            addCriterion("take_away_tel >=", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelLessThan(String value) {
            addCriterion("take_away_tel <", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelLessThanOrEqualTo(String value) {
            addCriterion("take_away_tel <=", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelLike(String value) {
            addCriterion("take_away_tel like", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelNotLike(String value) {
            addCriterion("take_away_tel not like", value, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelIn(List<String> values) {
            addCriterion("take_away_tel in", values, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelNotIn(List<String> values) {
            addCriterion("take_away_tel not in", values, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelBetween(String value1, String value2) {
            addCriterion("take_away_tel between", value1, value2, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andTakeAwayTelNotBetween(String value1, String value2) {
            addCriterion("take_away_tel not between", value1, value2, "takeAwayTel");
            return (Criteria) this;
        }

        public Criteria andPickUpNumIsNull() {
            addCriterion("pick_up_num is null");
            return (Criteria) this;
        }

        public Criteria andPickUpNumIsNotNull() {
            addCriterion("pick_up_num is not null");
            return (Criteria) this;
        }

        public Criteria andPickUpNumEqualTo(String value) {
            addCriterion("pick_up_num =", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumNotEqualTo(String value) {
            addCriterion("pick_up_num <>", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumGreaterThan(String value) {
            addCriterion("pick_up_num >", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumGreaterThanOrEqualTo(String value) {
            addCriterion("pick_up_num >=", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumLessThan(String value) {
            addCriterion("pick_up_num <", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumLessThanOrEqualTo(String value) {
            addCriterion("pick_up_num <=", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumLike(String value) {
            addCriterion("pick_up_num like", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumNotLike(String value) {
            addCriterion("pick_up_num not like", value, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumIn(List<String> values) {
            addCriterion("pick_up_num in", values, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumNotIn(List<String> values) {
            addCriterion("pick_up_num not in", values, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumBetween(String value1, String value2) {
            addCriterion("pick_up_num between", value1, value2, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andPickUpNumNotBetween(String value1, String value2) {
            addCriterion("pick_up_num not between", value1, value2, "pickUpNum");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressIsNull() {
            addCriterion("take_away_address is null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressIsNotNull() {
            addCriterion("take_away_address is not null");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressEqualTo(String value) {
            addCriterion("take_away_address =", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressNotEqualTo(String value) {
            addCriterion("take_away_address <>", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressGreaterThan(String value) {
            addCriterion("take_away_address >", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressGreaterThanOrEqualTo(String value) {
            addCriterion("take_away_address >=", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressLessThan(String value) {
            addCriterion("take_away_address <", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressLessThanOrEqualTo(String value) {
            addCriterion("take_away_address <=", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressLike(String value) {
            addCriterion("take_away_address like", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressNotLike(String value) {
            addCriterion("take_away_address not like", value, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressIn(List<String> values) {
            addCriterion("take_away_address in", values, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressNotIn(List<String> values) {
            addCriterion("take_away_address not in", values, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressBetween(String value1, String value2) {
            addCriterion("take_away_address between", value1, value2, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andTakeAwayAddressNotBetween(String value1, String value2) {
            addCriterion("take_away_address not between", value1, value2, "takeAwayAddress");
            return (Criteria) this;
        }

        public Criteria andStorePhoneIsNull() {
            addCriterion("store_phone is null");
            return (Criteria) this;
        }

        public Criteria andStorePhoneIsNotNull() {
            addCriterion("store_phone is not null");
            return (Criteria) this;
        }

        public Criteria andStorePhoneEqualTo(String value) {
            addCriterion("store_phone =", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneNotEqualTo(String value) {
            addCriterion("store_phone <>", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneGreaterThan(String value) {
            addCriterion("store_phone >", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneGreaterThanOrEqualTo(String value) {
            addCriterion("store_phone >=", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneLessThan(String value) {
            addCriterion("store_phone <", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneLessThanOrEqualTo(String value) {
            addCriterion("store_phone <=", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneLike(String value) {
            addCriterion("store_phone like", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneNotLike(String value) {
            addCriterion("store_phone not like", value, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneIn(List<String> values) {
            addCriterion("store_phone in", values, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneNotIn(List<String> values) {
            addCriterion("store_phone not in", values, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneBetween(String value1, String value2) {
            addCriterion("store_phone between", value1, value2, "storePhone");
            return (Criteria) this;
        }

        public Criteria andStorePhoneNotBetween(String value1, String value2) {
            addCriterion("store_phone not between", value1, value2, "storePhone");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeIsNull() {
            addCriterion("minimum_delivery_fee is null");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeIsNotNull() {
            addCriterion("minimum_delivery_fee is not null");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeEqualTo(Long value) {
            addCriterion("minimum_delivery_fee =", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeNotEqualTo(Long value) {
            addCriterion("minimum_delivery_fee <>", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeGreaterThan(Long value) {
            addCriterion("minimum_delivery_fee >", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeGreaterThanOrEqualTo(Long value) {
            addCriterion("minimum_delivery_fee >=", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeLessThan(Long value) {
            addCriterion("minimum_delivery_fee <", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeLessThanOrEqualTo(Long value) {
            addCriterion("minimum_delivery_fee <=", value, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeIn(List<Long> values) {
            addCriterion("minimum_delivery_fee in", values, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeNotIn(List<Long> values) {
            addCriterion("minimum_delivery_fee not in", values, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeBetween(Long value1, Long value2) {
            addCriterion("minimum_delivery_fee between", value1, value2, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andMinimumDeliveryFeeNotBetween(Long value1, Long value2) {
            addCriterion("minimum_delivery_fee not between", value1, value2, "minimumDeliveryFee");
            return (Criteria) this;
        }

        public Criteria andPackingChargeIsNull() {
            addCriterion("packing_charge is null");
            return (Criteria) this;
        }

        public Criteria andPackingChargeIsNotNull() {
            addCriterion("packing_charge is not null");
            return (Criteria) this;
        }

        public Criteria andPackingChargeEqualTo(Long value) {
            addCriterion("packing_charge =", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeNotEqualTo(Long value) {
            addCriterion("packing_charge <>", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeGreaterThan(Long value) {
            addCriterion("packing_charge >", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeGreaterThanOrEqualTo(Long value) {
            addCriterion("packing_charge >=", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeLessThan(Long value) {
            addCriterion("packing_charge <", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeLessThanOrEqualTo(Long value) {
            addCriterion("packing_charge <=", value, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeIn(List<Long> values) {
            addCriterion("packing_charge in", values, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeNotIn(List<Long> values) {
            addCriterion("packing_charge not in", values, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeBetween(Long value1, Long value2) {
            addCriterion("packing_charge between", value1, value2, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andPackingChargeNotBetween(Long value1, Long value2) {
            addCriterion("packing_charge not between", value1, value2, "packingCharge");
            return (Criteria) this;
        }

        public Criteria andTableWareNumIsNull() {
            addCriterion("table_ware_num is null");
            return (Criteria) this;
        }

        public Criteria andTableWareNumIsNotNull() {
            addCriterion("table_ware_num is not null");
            return (Criteria) this;
        }

        public Criteria andTableWareNumEqualTo(Integer value) {
            addCriterion("table_ware_num =", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumNotEqualTo(Integer value) {
            addCriterion("table_ware_num <>", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumGreaterThan(Integer value) {
            addCriterion("table_ware_num >", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("table_ware_num >=", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumLessThan(Integer value) {
            addCriterion("table_ware_num <", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumLessThanOrEqualTo(Integer value) {
            addCriterion("table_ware_num <=", value, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumIn(List<Integer> values) {
            addCriterion("table_ware_num in", values, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumNotIn(List<Integer> values) {
            addCriterion("table_ware_num not in", values, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumBetween(Integer value1, Integer value2) {
            addCriterion("table_ware_num between", value1, value2, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andTableWareNumNotBetween(Integer value1, Integer value2) {
            addCriterion("table_ware_num not between", value1, value2, "tableWareNum");
            return (Criteria) this;
        }

        public Criteria andOpenIdIsNull() {
            addCriterion("open_id is null");
            return (Criteria) this;
        }

        public Criteria andOpenIdIsNotNull() {
            addCriterion("open_id is not null");
            return (Criteria) this;
        }

        public Criteria andOpenIdEqualTo(String value) {
            addCriterion("open_id =", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotEqualTo(String value) {
            addCriterion("open_id <>", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdGreaterThan(String value) {
            addCriterion("open_id >", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdGreaterThanOrEqualTo(String value) {
            addCriterion("open_id >=", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLessThan(String value) {
            addCriterion("open_id <", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLessThanOrEqualTo(String value) {
            addCriterion("open_id <=", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLike(String value) {
            addCriterion("open_id like", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotLike(String value) {
            addCriterion("open_id not like", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdIn(List<String> values) {
            addCriterion("open_id in", values, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotIn(List<String> values) {
            addCriterion("open_id not in", values, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdBetween(String value1, String value2) {
            addCriterion("open_id between", value1, value2, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotBetween(String value1, String value2) {
            addCriterion("open_id not between", value1, value2, "openId");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneIsNull() {
            addCriterion("delivery_phone is null");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneIsNotNull() {
            addCriterion("delivery_phone is not null");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneEqualTo(String value) {
            addCriterion("delivery_phone =", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneNotEqualTo(String value) {
            addCriterion("delivery_phone <>", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneGreaterThan(String value) {
            addCriterion("delivery_phone >", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("delivery_phone >=", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneLessThan(String value) {
            addCriterion("delivery_phone <", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneLessThanOrEqualTo(String value) {
            addCriterion("delivery_phone <=", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneLike(String value) {
            addCriterion("delivery_phone like", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneNotLike(String value) {
            addCriterion("delivery_phone not like", value, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneIn(List<String> values) {
            addCriterion("delivery_phone in", values, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneNotIn(List<String> values) {
            addCriterion("delivery_phone not in", values, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneBetween(String value1, String value2) {
            addCriterion("delivery_phone between", value1, value2, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryPhoneNotBetween(String value1, String value2) {
            addCriterion("delivery_phone not between", value1, value2, "deliveryPhone");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameIsNull() {
            addCriterion("delivery_name is null");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameIsNotNull() {
            addCriterion("delivery_name is not null");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameEqualTo(String value) {
            addCriterion("delivery_name =", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameNotEqualTo(String value) {
            addCriterion("delivery_name <>", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameGreaterThan(String value) {
            addCriterion("delivery_name >", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameGreaterThanOrEqualTo(String value) {
            addCriterion("delivery_name >=", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameLessThan(String value) {
            addCriterion("delivery_name <", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameLessThanOrEqualTo(String value) {
            addCriterion("delivery_name <=", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameLike(String value) {
            addCriterion("delivery_name like", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameNotLike(String value) {
            addCriterion("delivery_name not like", value, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameIn(List<String> values) {
            addCriterion("delivery_name in", values, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameNotIn(List<String> values) {
            addCriterion("delivery_name not in", values, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameBetween(String value1, String value2) {
            addCriterion("delivery_name between", value1, value2, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andDeliveryNameNotBetween(String value1, String value2) {
            addCriterion("delivery_name not between", value1, value2, "deliveryName");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeIsNull() {
            addCriterion("appointment_time is null");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeIsNotNull() {
            addCriterion("appointment_time is not null");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeEqualTo(Date value) {
            addCriterion("appointment_time =", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeNotEqualTo(Date value) {
            addCriterion("appointment_time <>", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeGreaterThan(Date value) {
            addCriterion("appointment_time >", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("appointment_time >=", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeLessThan(Date value) {
            addCriterion("appointment_time <", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeLessThanOrEqualTo(Date value) {
            addCriterion("appointment_time <=", value, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeIn(List<Date> values) {
            addCriterion("appointment_time in", values, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeNotIn(List<Date> values) {
            addCriterion("appointment_time not in", values, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeBetween(Date value1, Date value2) {
            addCriterion("appointment_time between", value1, value2, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andAppointmentTimeNotBetween(Date value1, Date value2) {
            addCriterion("appointment_time not between", value1, value2, "appointmentTime");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdIsNull() {
            addCriterion("user_coupon_id is null");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdIsNotNull() {
            addCriterion("user_coupon_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdEqualTo(Long value) {
            addCriterion("user_coupon_id =", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdNotEqualTo(Long value) {
            addCriterion("user_coupon_id <>", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdGreaterThan(Long value) {
            addCriterion("user_coupon_id >", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_coupon_id >=", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdLessThan(Long value) {
            addCriterion("user_coupon_id <", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdLessThanOrEqualTo(Long value) {
            addCriterion("user_coupon_id <=", value, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdIn(List<Long> values) {
            addCriterion("user_coupon_id in", values, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdNotIn(List<Long> values) {
            addCriterion("user_coupon_id not in", values, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdBetween(Long value1, Long value2) {
            addCriterion("user_coupon_id between", value1, value2, "userCouponId");
            return (Criteria) this;
        }

        public Criteria andUserCouponIdNotBetween(Long value1, Long value2) {
            addCriterion("user_coupon_id not between", value1, value2, "userCouponId");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}