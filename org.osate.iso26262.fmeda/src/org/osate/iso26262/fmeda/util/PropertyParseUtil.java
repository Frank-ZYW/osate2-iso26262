package org.osate.iso26262.fmeda.util;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.BasicPropertyAssociation;
import org.osate.aadl2.BooleanLiteral;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.PropertyExpression;
import org.osate.aadl2.RealLiteral;
import org.osate.aadl2.RecordValue;
import org.osate.aadl2.StringLiteral;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;

public class PropertyParseUtil {

	/**
	 * Parse property with String type
	**/
	public static String getStringProperty(final EList<BasicPropertyAssociation> fields, String propertyName,
			String defaultValue) {
		String str = defaultValue;

		for (BasicPropertyAssociation bpa : fields) {
			if (bpa.getProperty().getName().equalsIgnoreCase(propertyName)) {
				PropertyExpression bva = EMV2Properties.getPropertyValue(bpa);
				StringLiteral sl = (StringLiteral) bva;
				str = sl.getValue();
			}
		}
		return str;
	}

	/**
	 * Parse property with Double type
	**/
	public static double getDoubleProperty(final EList<BasicPropertyAssociation> fields, String propertyName,
			double defaultValue) {
		double db = defaultValue;

		for (BasicPropertyAssociation bpa : fields) {
			if (bpa.getProperty().getName().equalsIgnoreCase(propertyName)) {
				PropertyExpression bva = EMV2Properties.getPropertyValue(bpa);
				RealLiteral rl = (RealLiteral) bva;
				db = rl.getScaledValue();
			}
		}
		return db;
	}

	/**
	 * Parse property with Boolean type
	**/
	public static Boolean getBooleanProperty(final EList<BasicPropertyAssociation> fields, String propertyName,
			boolean defaultValue) {
		Boolean bool = defaultValue;

		for (BasicPropertyAssociation bpa : fields) {
			if (bpa.getProperty().getName().equalsIgnoreCase(propertyName)) {
				PropertyExpression bva = EMV2Properties.getPropertyValue(bpa);
				BooleanLiteral bl = (BooleanLiteral) bva;
				bool = bl.getValue();
			}
		}
		return bool;
	}

	/**
	 * Get property field
	**/
	public static EList<BasicPropertyAssociation> getPropertyField(String propertyName, NamedElement ci,
			NamedElement target, ErrorTypes ts) {

		List<EMV2PropertyAssociation> properties = EMV2Properties.getProperty(propertyName, ci, target, ts);
		EMV2PropertyAssociation property = properties.isEmpty() ? null : properties.get(0);

		if (property != null) {
			PropertyExpression pVal = EMV2Properties.getPropertyValue(property);
			RecordValue rpVal = (RecordValue) pVal;
			EList<BasicPropertyAssociation> pFields = rpVal.getOwnedFieldValues();
			return pFields;
		}
		return null;
	}

}
