package com.htyoudao.youdao.module.order.controller.orderextend.VO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BzOrderExtendExample {

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

    public BzOrderExtendExample() {
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

        public Criteria andExtendIdIsNull() {
            addCriterion("extend_id is null");
            return (Criteria) this;
        }

        public Criteria andExtendIdIsNotNull() {
            addCriterion("extend_id is not null");
            return (Criteria) this;
        }

        public Criteria andExtendIdEqualTo(Integer value) {
            addCriterion("extend_id =", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdNotEqualTo(Integer value) {
            addCriterion("extend_id <>", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdGreaterThan(Integer value) {
            addCriterion("extend_id >", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("extend_id >=", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdLessThan(Integer value) {
            addCriterion("extend_id <", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdLessThanOrEqualTo(Integer value) {
            addCriterion("extend_id <=", value, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdIn(List<Integer> values) {
            addCriterion("extend_id in", values, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdNotIn(List<Integer> values) {
            addCriterion("extend_id not in", values, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdBetween(Integer value1, Integer value2) {
            addCriterion("extend_id between", value1, value2, "extendId");
            return (Criteria) this;
        }

        public Criteria andExtendIdNotBetween(Integer value1, Integer value2) {
            addCriterion("extend_id not between", value1, value2, "extendId");
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

        public Criteria andDeliverTypeIsNull() {
            addCriterion("deliver_type is null");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeIsNotNull() {
            addCriterion("deliver_type is not null");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeEqualTo(Byte value) {
            addCriterion("deliver_type =", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeNotEqualTo(Byte value) {
            addCriterion("deliver_type <>", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeGreaterThan(Byte value) {
            addCriterion("deliver_type >", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("deliver_type >=", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeLessThan(Byte value) {
            addCriterion("deliver_type <", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeLessThanOrEqualTo(Byte value) {
            addCriterion("deliver_type <=", value, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeIn(List<Byte> values) {
            addCriterion("deliver_type in", values, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeNotIn(List<Byte> values) {
            addCriterion("deliver_type not in", values, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeBetween(Byte value1, Byte value2) {
            addCriterion("deliver_type between", value1, value2, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("deliver_type not between", value1, value2, "deliverType");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileIsNull() {
            addCriterion("deliver_mobile is null");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileIsNotNull() {
            addCriterion("deliver_mobile is not null");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileEqualTo(String value) {
            addCriterion("deliver_mobile =", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileNotEqualTo(String value) {
            addCriterion("deliver_mobile <>", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileGreaterThan(String value) {
            addCriterion("deliver_mobile >", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileGreaterThanOrEqualTo(String value) {
            addCriterion("deliver_mobile >=", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileLessThan(String value) {
            addCriterion("deliver_mobile <", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileLessThanOrEqualTo(String value) {
            addCriterion("deliver_mobile <=", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileLike(String value) {
            addCriterion("deliver_mobile like", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileNotLike(String value) {
            addCriterion("deliver_mobile not like", value, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileIn(List<String> values) {
            addCriterion("deliver_mobile in", values, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileNotIn(List<String> values) {
            addCriterion("deliver_mobile not in", values, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileBetween(String value1, String value2) {
            addCriterion("deliver_mobile between", value1, value2, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverMobileNotBetween(String value1, String value2) {
            addCriterion("deliver_mobile not between", value1, value2, "deliverMobile");
            return (Criteria) this;
        }

        public Criteria andDeliverNameIsNull() {
            addCriterion("deliver_name is null");
            return (Criteria) this;
        }

        public Criteria andDeliverNameIsNotNull() {
            addCriterion("deliver_name is not null");
            return (Criteria) this;
        }

        public Criteria andDeliverNameEqualTo(String value) {
            addCriterion("deliver_name =", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameNotEqualTo(String value) {
            addCriterion("deliver_name <>", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameGreaterThan(String value) {
            addCriterion("deliver_name >", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameGreaterThanOrEqualTo(String value) {
            addCriterion("deliver_name >=", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameLessThan(String value) {
            addCriterion("deliver_name <", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameLessThanOrEqualTo(String value) {
            addCriterion("deliver_name <=", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameLike(String value) {
            addCriterion("deliver_name like", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameNotLike(String value) {
            addCriterion("deliver_name not like", value, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameIn(List<String> values) {
            addCriterion("deliver_name in", values, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameNotIn(List<String> values) {
            addCriterion("deliver_name not in", values, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameBetween(String value1, String value2) {
            addCriterion("deliver_name between", value1, value2, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverNameNotBetween(String value1, String value2) {
            addCriterion("deliver_name not between", value1, value2, "deliverName");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeIsNull() {
            addCriterion("deliver_time is null");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeIsNotNull() {
            addCriterion("deliver_time is not null");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeEqualTo(Date value) {
            addCriterion("deliver_time =", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeNotEqualTo(Date value) {
            addCriterion("deliver_time <>", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeGreaterThan(Date value) {
            addCriterion("deliver_time >", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("deliver_time >=", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeLessThan(Date value) {
            addCriterion("deliver_time <", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeLessThanOrEqualTo(Date value) {
            addCriterion("deliver_time <=", value, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeIn(List<Date> values) {
            addCriterion("deliver_time in", values, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeNotIn(List<Date> values) {
            addCriterion("deliver_time not in", values, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeBetween(Date value1, Date value2) {
            addCriterion("deliver_time between", value1, value2, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andDeliverTimeNotBetween(Date value1, Date value2) {
            addCriterion("deliver_time not between", value1, value2, "deliverTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeIsNull() {
            addCriterion("evaluation_time is null");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeIsNotNull() {
            addCriterion("evaluation_time is not null");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeEqualTo(Date value) {
            addCriterion("evaluation_time =", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeNotEqualTo(Date value) {
            addCriterion("evaluation_time <>", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeGreaterThan(Date value) {
            addCriterion("evaluation_time >", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("evaluation_time >=", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeLessThan(Date value) {
            addCriterion("evaluation_time <", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeLessThanOrEqualTo(Date value) {
            addCriterion("evaluation_time <=", value, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeIn(List<Date> values) {
            addCriterion("evaluation_time in", values, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeNotIn(List<Date> values) {
            addCriterion("evaluation_time not in", values, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeBetween(Date value1, Date value2) {
            addCriterion("evaluation_time between", value1, value2, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andEvaluationTimeNotBetween(Date value1, Date value2) {
            addCriterion("evaluation_time not between", value1, value2, "evaluationTime");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkIsNull() {
            addCriterion("order_remark is null");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkIsNotNull() {
            addCriterion("order_remark is not null");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkEqualTo(String value) {
            addCriterion("order_remark =", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkNotEqualTo(String value) {
            addCriterion("order_remark <>", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkGreaterThan(String value) {
            addCriterion("order_remark >", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("order_remark >=", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkLessThan(String value) {
            addCriterion("order_remark <", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkLessThanOrEqualTo(String value) {
            addCriterion("order_remark <=", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkLike(String value) {
            addCriterion("order_remark like", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkNotLike(String value) {
            addCriterion("order_remark not like", value, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkIn(List<String> values) {
            addCriterion("order_remark in", values, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkNotIn(List<String> values) {
            addCriterion("order_remark not in", values, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkBetween(String value1, String value2) {
            addCriterion("order_remark between", value1, value2, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderRemarkNotBetween(String value1, String value2) {
            addCriterion("order_remark not between", value1, value2, "orderRemark");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountIsNull() {
            addCriterion("order_points_count is null");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountIsNotNull() {
            addCriterion("order_points_count is not null");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountEqualTo(Integer value) {
            addCriterion("order_points_count =", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountNotEqualTo(Integer value) {
            addCriterion("order_points_count <>", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountGreaterThan(Integer value) {
            addCriterion("order_points_count >", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("order_points_count >=", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountLessThan(Integer value) {
            addCriterion("order_points_count <", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountLessThanOrEqualTo(Integer value) {
            addCriterion("order_points_count <=", value, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountIn(List<Integer> values) {
            addCriterion("order_points_count in", values, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountNotIn(List<Integer> values) {
            addCriterion("order_points_count not in", values, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountBetween(Integer value1, Integer value2) {
            addCriterion("order_points_count between", value1, value2, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andOrderPointsCountNotBetween(Integer value1, Integer value2) {
            addCriterion("order_points_count not between", value1, value2, "orderPointsCount");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceIsNull() {
            addCriterion("voucher_price is null");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceIsNotNull() {
            addCriterion("voucher_price is not null");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceEqualTo(BigDecimal value) {
            addCriterion("voucher_price =", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceNotEqualTo(BigDecimal value) {
            addCriterion("voucher_price <>", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceGreaterThan(BigDecimal value) {
            addCriterion("voucher_price >", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("voucher_price >=", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceLessThan(BigDecimal value) {
            addCriterion("voucher_price <", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("voucher_price <=", value, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceIn(List<BigDecimal> values) {
            addCriterion("voucher_price in", values, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceNotIn(List<BigDecimal> values) {
            addCriterion("voucher_price not in", values, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("voucher_price between", value1, value2, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherPriceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("voucher_price not between", value1, value2, "voucherPrice");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeIsNull() {
            addCriterion("voucher_code is null");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeIsNotNull() {
            addCriterion("voucher_code is not null");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeEqualTo(String value) {
            addCriterion("voucher_code =", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeNotEqualTo(String value) {
            addCriterion("voucher_code <>", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeGreaterThan(String value) {
            addCriterion("voucher_code >", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeGreaterThanOrEqualTo(String value) {
            addCriterion("voucher_code >=", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeLessThan(String value) {
            addCriterion("voucher_code <", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeLessThanOrEqualTo(String value) {
            addCriterion("voucher_code <=", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeLike(String value) {
            addCriterion("voucher_code like", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeNotLike(String value) {
            addCriterion("voucher_code not like", value, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeIn(List<String> values) {
            addCriterion("voucher_code in", values, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeNotIn(List<String> values) {
            addCriterion("voucher_code not in", values, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeBetween(String value1, String value2) {
            addCriterion("voucher_code between", value1, value2, "voucherCode");
            return (Criteria) this;
        }

        public Criteria andVoucherCodeNotBetween(String value1, String value2) {
            addCriterion("voucher_code not between", value1, value2, "voucherCode");
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

        public Criteria andDeliverAddressIdIsNull() {
            addCriterion("deliver_address_id is null");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdIsNotNull() {
            addCriterion("deliver_address_id is not null");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdEqualTo(Integer value) {
            addCriterion("deliver_address_id =", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdNotEqualTo(Integer value) {
            addCriterion("deliver_address_id <>", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdGreaterThan(Integer value) {
            addCriterion("deliver_address_id >", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("deliver_address_id >=", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdLessThan(Integer value) {
            addCriterion("deliver_address_id <", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdLessThanOrEqualTo(Integer value) {
            addCriterion("deliver_address_id <=", value, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdIn(List<Integer> values) {
            addCriterion("deliver_address_id in", values, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdNotIn(List<Integer> values) {
            addCriterion("deliver_address_id not in", values, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdBetween(Integer value1, Integer value2) {
            addCriterion("deliver_address_id between", value1, value2, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andDeliverAddressIdNotBetween(Integer value1, Integer value2) {
            addCriterion("deliver_address_id not between", value1, value2, "deliverAddressId");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeIsNull() {
            addCriterion("receiver_province_code is null");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeIsNotNull() {
            addCriterion("receiver_province_code is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeEqualTo(String value) {
            addCriterion("receiver_province_code =", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeNotEqualTo(String value) {
            addCriterion("receiver_province_code <>", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeGreaterThan(String value) {
            addCriterion("receiver_province_code >", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_province_code >=", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeLessThan(String value) {
            addCriterion("receiver_province_code <", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeLessThanOrEqualTo(String value) {
            addCriterion("receiver_province_code <=", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeLike(String value) {
            addCriterion("receiver_province_code like", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeNotLike(String value) {
            addCriterion("receiver_province_code not like", value, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeIn(List<String> values) {
            addCriterion("receiver_province_code in", values, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeNotIn(List<String> values) {
            addCriterion("receiver_province_code not in", values, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeBetween(String value1, String value2) {
            addCriterion("receiver_province_code between", value1, value2, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverProvinceCodeNotBetween(String value1, String value2) {
            addCriterion("receiver_province_code not between", value1, value2, "receiverProvinceCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeIsNull() {
            addCriterion("receiver_city_code is null");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeIsNotNull() {
            addCriterion("receiver_city_code is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeEqualTo(String value) {
            addCriterion("receiver_city_code =", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeNotEqualTo(String value) {
            addCriterion("receiver_city_code <>", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeGreaterThan(String value) {
            addCriterion("receiver_city_code >", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_city_code >=", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeLessThan(String value) {
            addCriterion("receiver_city_code <", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeLessThanOrEqualTo(String value) {
            addCriterion("receiver_city_code <=", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeLike(String value) {
            addCriterion("receiver_city_code like", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeNotLike(String value) {
            addCriterion("receiver_city_code not like", value, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeIn(List<String> values) {
            addCriterion("receiver_city_code in", values, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeNotIn(List<String> values) {
            addCriterion("receiver_city_code not in", values, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeBetween(String value1, String value2) {
            addCriterion("receiver_city_code between", value1, value2, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverCityCodeNotBetween(String value1, String value2) {
            addCriterion("receiver_city_code not between", value1, value2, "receiverCityCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeIsNull() {
            addCriterion("receiver_district_code is null");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeIsNotNull() {
            addCriterion("receiver_district_code is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeEqualTo(String value) {
            addCriterion("receiver_district_code =", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeNotEqualTo(String value) {
            addCriterion("receiver_district_code <>", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeGreaterThan(String value) {
            addCriterion("receiver_district_code >", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_district_code >=", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeLessThan(String value) {
            addCriterion("receiver_district_code <", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeLessThanOrEqualTo(String value) {
            addCriterion("receiver_district_code <=", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeLike(String value) {
            addCriterion("receiver_district_code like", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeNotLike(String value) {
            addCriterion("receiver_district_code not like", value, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeIn(List<String> values) {
            addCriterion("receiver_district_code in", values, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeNotIn(List<String> values) {
            addCriterion("receiver_district_code not in", values, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeBetween(String value1, String value2) {
            addCriterion("receiver_district_code between", value1, value2, "receiverDistrictCode");
            return (Criteria) this;
        }

        public Criteria andReceiverDistrictCodeNotBetween(String value1, String value2) {
            addCriterion("receiver_district_code not between", value1, value2, "receiverDistrictCode");
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

        public Criteria andReceiverInfoIsNull() {
            addCriterion("receiver_info is null");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoIsNotNull() {
            addCriterion("receiver_info is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoEqualTo(String value) {
            addCriterion("receiver_info =", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoNotEqualTo(String value) {
            addCriterion("receiver_info <>", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoGreaterThan(String value) {
            addCriterion("receiver_info >", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_info >=", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoLessThan(String value) {
            addCriterion("receiver_info <", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoLessThanOrEqualTo(String value) {
            addCriterion("receiver_info <=", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoLike(String value) {
            addCriterion("receiver_info like", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoNotLike(String value) {
            addCriterion("receiver_info not like", value, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoIn(List<String> values) {
            addCriterion("receiver_info in", values, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoNotIn(List<String> values) {
            addCriterion("receiver_info not in", values, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoBetween(String value1, String value2) {
            addCriterion("receiver_info between", value1, value2, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andReceiverInfoNotBetween(String value1, String value2) {
            addCriterion("receiver_info not between", value1, value2, "receiverInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoIsNull() {
            addCriterion("invoice_info is null");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoIsNotNull() {
            addCriterion("invoice_info is not null");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoEqualTo(String value) {
            addCriterion("invoice_info =", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoNotEqualTo(String value) {
            addCriterion("invoice_info <>", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoGreaterThan(String value) {
            addCriterion("invoice_info >", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_info >=", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoLessThan(String value) {
            addCriterion("invoice_info <", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoLessThanOrEqualTo(String value) {
            addCriterion("invoice_info <=", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoLike(String value) {
            addCriterion("invoice_info like", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoNotLike(String value) {
            addCriterion("invoice_info not like", value, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoIn(List<String> values) {
            addCriterion("invoice_info in", values, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoNotIn(List<String> values) {
            addCriterion("invoice_info not in", values, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoBetween(String value1, String value2) {
            addCriterion("invoice_info between", value1, value2, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andInvoiceInfoNotBetween(String value1, String value2) {
            addCriterion("invoice_info not between", value1, value2, "invoiceInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoIsNull() {
            addCriterion("promotion_info is null");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoIsNotNull() {
            addCriterion("promotion_info is not null");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoEqualTo(String value) {
            addCriterion("promotion_info =", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoNotEqualTo(String value) {
            addCriterion("promotion_info <>", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoGreaterThan(String value) {
            addCriterion("promotion_info >", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoGreaterThanOrEqualTo(String value) {
            addCriterion("promotion_info >=", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoLessThan(String value) {
            addCriterion("promotion_info <", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoLessThanOrEqualTo(String value) {
            addCriterion("promotion_info <=", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoLike(String value) {
            addCriterion("promotion_info like", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoNotLike(String value) {
            addCriterion("promotion_info not like", value, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoIn(List<String> values) {
            addCriterion("promotion_info in", values, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoNotIn(List<String> values) {
            addCriterion("promotion_info not in", values, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoBetween(String value1, String value2) {
            addCriterion("promotion_info between", value1, value2, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andPromotionInfoNotBetween(String value1, String value2) {
            addCriterion("promotion_info not between", value1, value2, "promotionInfo");
            return (Criteria) this;
        }

        public Criteria andIsDzmdIsNull() {
            addCriterion("is_dzmd is null");
            return (Criteria) this;
        }

        public Criteria andIsDzmdIsNotNull() {
            addCriterion("is_dzmd is not null");
            return (Criteria) this;
        }

        public Criteria andIsDzmdEqualTo(Byte value) {
            addCriterion("is_dzmd =", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdNotEqualTo(Byte value) {
            addCriterion("is_dzmd <>", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdGreaterThan(Byte value) {
            addCriterion("is_dzmd >", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_dzmd >=", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdLessThan(Byte value) {
            addCriterion("is_dzmd <", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdLessThanOrEqualTo(Byte value) {
            addCriterion("is_dzmd <=", value, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdIn(List<Byte> values) {
            addCriterion("is_dzmd in", values, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdNotIn(List<Byte> values) {
            addCriterion("is_dzmd not in", values, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdBetween(Byte value1, Byte value2) {
            addCriterion("is_dzmd between", value1, value2, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andIsDzmdNotBetween(Byte value1, Byte value2) {
            addCriterion("is_dzmd not between", value1, value2, "isDzmd");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusIsNull() {
            addCriterion("invoice_status is null");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusIsNotNull() {
            addCriterion("invoice_status is not null");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusEqualTo(Byte value) {
            addCriterion("invoice_status =", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusNotEqualTo(Byte value) {
            addCriterion("invoice_status <>", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusGreaterThan(Byte value) {
            addCriterion("invoice_status >", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("invoice_status >=", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusLessThan(Byte value) {
            addCriterion("invoice_status <", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusLessThanOrEqualTo(Byte value) {
            addCriterion("invoice_status <=", value, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusIn(List<Byte> values) {
            addCriterion("invoice_status in", values, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusNotIn(List<Byte> values) {
            addCriterion("invoice_status not in", values, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusBetween(Byte value1, Byte value2) {
            addCriterion("invoice_status between", value1, value2, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andInvoiceStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("invoice_status not between", value1, value2, "invoiceStatus");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountIsNull() {
            addCriterion("platform_voucher_amount is null");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountIsNotNull() {
            addCriterion("platform_voucher_amount is not null");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountEqualTo(BigDecimal value) {
            addCriterion("platform_voucher_amount =", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountNotEqualTo(BigDecimal value) {
            addCriterion("platform_voucher_amount <>", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountGreaterThan(BigDecimal value) {
            addCriterion("platform_voucher_amount >", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("platform_voucher_amount >=", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountLessThan(BigDecimal value) {
            addCriterion("platform_voucher_amount <", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("platform_voucher_amount <=", value, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountIn(List<BigDecimal> values) {
            addCriterion("platform_voucher_amount in", values, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountNotIn(List<BigDecimal> values) {
            addCriterion("platform_voucher_amount not in", values, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("platform_voucher_amount between", value1, value2, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andPlatformVoucherAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("platform_voucher_amount not between", value1, value2, "platformVoucherAmount");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(String value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(String value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(String value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(String value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(String value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLike(String value) {
            addCriterion("task_id like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotLike(String value) {
            addCriterion("task_id not like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<String> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<String> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(String value1, String value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(String value1, String value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
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