package com.paymentgateway.commons.rule;

/**
 * @author Surender
 *
 */
public class RuleFactory {

	public RuleFactory() {
	}

	public static Rule instance(String operand1, String operand2,
			OperandType operandType, OperatorType operatorType) {
		return new Rule(operand1, operand2, operandType, operatorType);
	}

}
