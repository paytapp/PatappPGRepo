package com.paymentgateway.commons.rule;

import com.paymentgateway.commons.util.Fields;

/**
 * @author Surender
 *
 */
public class Evaluator {
	private Rule rule;
	private RuleStatusType statusType;

	public Evaluator() {
	}

	private RuleStatusType run(Rule rule, Fields fields) {
		if (statusType != RuleStatusType.PENDING) {
			return statusType;
		}

		if (null == rule) {
			return statusType = RuleStatusType.INVALID;
		}

		String field1Value = fields.get(rule.getField1());
		String field2Value = fields.get(rule.getField2());
		if (rule.getOperandType() == OperandType.NUMBER) {
			statusType = execute(Double.valueOf(field1Value),
					Double.valueOf(field2Value), rule.getOperatorType());
		} else if (rule.getOperandType() == OperandType.STRING) {
			statusType = execute(field1Value, field2Value,
					rule.getOperatorType());
		}

		if (RuleStatusType.TRUE == statusType && null != rule.getRule()) {
			statusType = run(rule.getRule(), fields);
		}

		return statusType;
	}

	public RuleStatusType execute(String value1, String value2,
			OperatorType operator) {
		RuleStatusType status = RuleStatusType.FALSE;

		switch (operator) {
		case EQUAL:
			if(value1.equals(value2)){
				status = RuleStatusType.TRUE;
			}
			
			break;
			
		case NOT_EQUAL_TO:
			if(!value1.equals(value2)){
				status = RuleStatusType.TRUE;
			}
			
		case GREATER_THAN:
			status = RuleStatusType.INVALID;
			break;
		case GREATER_THAN_EQUAL_TO:
			status = RuleStatusType.INVALID;
			break;
		case LESS_THAN:
			status = RuleStatusType.INVALID;
			break;
		case LESS_THAN_EQUAL_TO:
			status = RuleStatusType.INVALID;
			break;
		default:
			break;
		}
				
		return status;
	}

	public RuleStatusType execute(double value1, double value2,
			OperatorType operator) {
		RuleStatusType status = RuleStatusType.FALSE;

		switch (operator) {
		case EQUAL:
			if (value1 == value2) {
				status = RuleStatusType.TRUE;
			}

			break;
		case NOT_EQUAL_TO:
			if (value1 != value2) {
				status = RuleStatusType.TRUE;
			}
			
		case GREATER_THAN:
			if (value1 > value2) {
				status = RuleStatusType.TRUE;
			}

			break;
		case GREATER_THAN_EQUAL_TO:
			if (value1 >= value2) {
				status = RuleStatusType.TRUE;
			}

			break;
		case LESS_THAN:
			if (value1 < value2) {
				status = RuleStatusType.TRUE;
			}

			break;
		case LESS_THAN_EQUAL_TO:
			if (value1 >= value2) {
				status = RuleStatusType.TRUE;
			}

			break;
		default:
			break;
		}

		return status;
	}

	public RuleStatusType evaluate(Fields fields) {

		setStatusType(RuleStatusType.PENDING);
		run(rule, fields);

		return statusType;
	}

	public RuleStatusType evaluate(Rule rule, Fields fields) {
		setRule(rule);

		return evaluate(fields);
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public RuleStatusType getStatusType() {
		return statusType;
	}

	public void setStatusType(RuleStatusType statusType) {
		this.statusType = statusType;
	}
}
