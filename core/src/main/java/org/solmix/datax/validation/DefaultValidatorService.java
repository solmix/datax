/*
 * Copyright 2014 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.datax.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.DataUtils;
import org.solmix.commons.util.Reflection;
import org.solmix.datax.DataService;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.model.FieldInfo;
import org.solmix.datax.model.FieldType;
import org.solmix.datax.model.LookupType;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.script.VelocityExpression;
import org.solmix.datax.support.DSRequestResolver;
import org.solmix.datax.support.RequestContextResourceResolver;
import org.solmix.runtime.Container;
import org.solmix.runtime.bean.ConfiguredBeanProvider;
import org.solmix.runtime.resource.ResourceInjector;
import org.solmix.runtime.resource.ResourceManager;
import org.solmix.runtime.resource.support.ResourceManagerImpl;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月20日
 */

public class DefaultValidatorService implements ValidatorService
{

    static DefaultValidatorService instance = new DefaultValidatorService();

    private static Logger log = LoggerFactory.getLogger(DefaultValidatorService.class.getName());

    private static final List<String> clientOnlyValidators = DataUtils.makeList("requiredIf");

    static final Map<String, Validator> defaultValidators;
    static {
        defaultValidators = new HashMap<String, Validator>();
        defaultValidators.put("required", new required());
        defaultValidators.put("isOneOf", new isOneOf());
        defaultValidators.put("isBoolean", new isBoolean());
        defaultValidators.put("isInteger", new isInteger());
        defaultValidators.put("isDate", new isDate());
        defaultValidators.put("isTime", new isTime());
        defaultValidators.put("isFloat", new isFloat());
        defaultValidators.put("isIdentifier", new isIdentifier());
        defaultValidators.put("isURL", new isURL());
        defaultValidators.put("isString", new isString());
        defaultValidators.put("isRegexp", new isRegexp());
        defaultValidators.put("isUnique", new isUnique());
        defaultValidators.put("integerRange", new integerRange());
        defaultValidators.put("regex", new regex());
        defaultValidators.put("lengthRange", new lengthRange());
        defaultValidators.put("matchesField", new matchesField());
        defaultValidators.put("contains", new contains());
        defaultValidators.put("doesntContain", new doesntContain());
        defaultValidators.put("substringCount", new substringCount());
        defaultValidators.put("mask", new mask());
        defaultValidators.put("floatLimit", new floatLimit());
        defaultValidators.put("floatPrecision", new floatPrecision());
        defaultValidators.put("floatRange", new floatRange());
        defaultValidators.put("integerOrAuto", new integerOrAuto());
        defaultValidators.put("custom", new custom());
        defaultValidators.put("hasRelatedRecord", new hasRelatedRecord());
    }

    final static Map<FieldType, ValidatorInfo> buildInValidator;
    static {
        buildInValidator = new HashMap<FieldType, ValidatorInfo>();
        buildInValidator.put(FieldType.TEXT, new ValidatorInfo("isString"));
        buildInValidator.put(FieldType.BOOLEAN, new ValidatorInfo("isBoolean"));
        buildInValidator.put(FieldType.INTEGER, new ValidatorInfo("isInteger"));
        buildInValidator.put(FieldType.FLOAT, new ValidatorInfo("isFloat"));
        buildInValidator.put(FieldType.DATE, new ValidatorInfo("isDate"));
        buildInValidator.put(FieldType.TIME, new ValidatorInfo("isTime"));
        buildInValidator.put(FieldType.DATETIME, new ValidatorInfo("isDate"));
        buildInValidator.put(FieldType.ENUM, null);
        buildInValidator.put(FieldType.INT_ENUM, new ValidatorInfo("isInteger"));
        buildInValidator.put(FieldType.SEQUENCE, new ValidatorInfo("isInteger"));
        buildInValidator.put(FieldType.LINK, null);
        buildInValidator.put(FieldType.IMAGE, null);
        buildInValidator.put(FieldType.BINARY, null);
        buildInValidator.put(FieldType.IMAGE_FILE, null);
        buildInValidator.put(FieldType.MODIFIER, null);
        buildInValidator.put(FieldType.MODIFIER_TIMESTAMP, null);
        buildInValidator.put(FieldType.PASSWORD, null);
    }

    private DefaultValidatorService()
    {

    }

    public static DefaultValidatorService getInstance() {
        return instance;
    }

    static class custom implements Validator
    {

        @SuppressWarnings("unchecked")
        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
           
            
         
          Object rawResult = null;
          String expression=  validatorInfo.getExpression();
          //velocity表达式验证
          if(expression!=null){
              Map<String, Object> template=  context.getTemplateContext();
              VelocityExpression velocityExpression;
              Map<String,Object> vcontext;
              if(template.get(VelocityExpression.class.getName())!=null){
                  velocityExpression=(VelocityExpression) template.get(VelocityExpression.class.getName());
                  vcontext=(Map<String,Object>)template.get(VelocityContext.class.getName());
              }else{
                  velocityExpression=new VelocityExpression(context.getContainer());
                  vcontext=velocityExpression.prepareContext(context.getDSRequest(), context.getRequestContext());
                  context.addToTemplateContext(VelocityExpression.class.getName(), velocityExpression);
                  context.addToTemplateContext(VelocityContext.class.getName(),vcontext);
              }
              rawResult=velocityExpression.evaluateValue(expression, vcontext);
              Boolean rtnValue= DataUtils.asBooleanObject(rawResult);
              if (Boolean.TRUE.equals(rtnValue))
                  return null;
              else
                  return new ErrorMessage(getErrorString(validatorInfo, "Failed custom validation"));
         
          }else {
              Container container = context.getContainer();
              Validator validator=null;
              Class<? extends Validator> serviceClass= validatorInfo.getClazz();
              if(validatorInfo.getLookup()==LookupType.CONTAINER){
                  String serviceName= validatorInfo.getName();
                  if(serviceName==null){
                      if(serviceClass==null||serviceClass==Validator.class){
                          //通常有多个validator
                          throw new ValidationException("custom validator must specify name to determine validator service ");
                      }else{
                          validator=container.getExtension(serviceClass);
                      }
                      
                  }else{
                      ConfiguredBeanProvider provider = container.getExtension(ConfiguredBeanProvider.class);
                      if(provider!=null){
                          validator= provider.getBeanOfType(serviceName, serviceClass==null?Validator.class:serviceClass);
                      }
                  }
              } else if(serviceClass!=null&&validatorInfo.getLookup()==LookupType.NEW){
                  try {
                      validator = Reflection.newInstance(serviceClass);
                  } catch (Exception e) {
                     throw new ValidationException("Instance object",e);
                  }
              }
              if(validator!=null){
                  ResourceManager rma= container.getExtension(ResourceManager.class);
                  ResourceManagerImpl rm = new ResourceManagerImpl(rma.getResourceResolvers());
                  rm.addResourceResolver( new RequestContextResourceResolver(context.getRequestContext()));
                  rm.addResourceResolver( new DSRequestResolver(context.getDSRequest()));
                  ResourceInjector injector = new ResourceInjector(rm);
                  injector.injectAware(validator);
                  injector.inject(validator);
                  injector.construct(validator);
              }
             return validator.validate(validatorInfo, value, fieldName, record, context);
          }
        }
    }
    
    static class hasRelatedRecord implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
           
            Container container = context.getContainer();
            DataServiceManager manager=null;
            if(container!=null){
                manager = container.getExtension(DataServiceManager.class);
            }
            if(manager==null){
                return null;
            }
            Map<String,Object> params = context.getTemplateContext();
            String relatedDS = (String)validatorInfo.getProperty("RelatedDataService");
            String relatedField = (String)validatorInfo.getProperty("RelatedField");
            if (relatedDS == null || relatedField == null) {
                FieldInfo thisField = (FieldInfo) params.get(ValidationContext.TEMPLATE_FIELD);
                String fk = thisField.getForeignKey();
                if (fk != null) {
                    String tokens[] = fk.split("[.]");
                    if (tokens.length == 1) {
                        if (relatedDS == null)
                            relatedDS = ((DataService) params.get(ValidationContext.TEMPLATE_DATASERVICE)).getId();
                        if (relatedField == null)
                            relatedField = tokens[0];
                    } else {
                        if (relatedDS == null)
                            relatedDS = tokens[0];
                        if (relatedField == null)
                            relatedField = tokens[1];
                    }
                }
            }
            if (relatedDS == null || relatedField == null) {
                String error = (new StringBuilder()).append("Field ").append(fieldName).append(" - 'hasRelatedRecord' validation could not derive ").append(
                    "a relation to test - specify 'relatedDataSource' and 'relatedField' on ").append(
                    "the validator, or a foreignKey property in this field's DataSource ").append("definition.  Cannot proceed, assuming false.").toString();
                log.warn(error);
                return new ErrorMessage(error);
            }
            DataService ds;
            try {
                Container c = context.getContainer();
                if(c!=null){
                    
                }
                ds = manager.getDataService(relatedDS);
                if (ds == null) {
                    String error = (new StringBuilder()).append("Field ")
                        .append(fieldName)
                        .append(" - 'hasRelatedRecord' validation encountered a ")
                        .append("'relatedDataSource' that was not a real DataSource.  Please check ")
                        .append("your validator code.  Unable to proceed, assuming false.")
                        .toString();
                    log.warn(error);
                    return new ErrorMessage(error,null,value);
                }
            } catch (Exception e) {
                return new ErrorMessage(e.getMessage(),null,value);
            }
            try {
                if (!ds.hasRecord(relatedField, value))
                    return new ErrorMessage(getErrorString(validatorInfo, "Related record does not exist"),null,value);
            } catch (Exception e) {
                throw new ValidationException(e);
            }
            return null;
        }
    }
    
    static class integerOrAuto implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            if ("auto".equalsIgnoreCase(value.toString()))
                return null;
            return getBuiltinValidator("isInteger").validate(validatorInfo, value, fieldName, record, context);
        }
    }
    
    static class floatRange implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            double num;
            try {
                num = Double.valueOf(value.toString()).doubleValue();
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "Must be a valid decimal."));
            }
            Double min = validatorInfo.getMin();
            Double max = validatorInfo.getMax();
            if (max != null && num > max.doubleValue())
                return new ErrorMessage(getErrorString(validatorInfo), max);
            if (min != null && num < min.doubleValue())
                return new ErrorMessage(getErrorString(validatorInfo), min);
            else
                return null;
        }
    }

    static class floatPrecision implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo, Object value, String fieldName, Map<String, Object> record,
            ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            double num;
            try {
                num = Double.valueOf(value.toString()).doubleValue();
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "Must be a valid decimal."));
            }
            Double precision = validatorInfo.getPrecision();
            if (precision != null) {
                double multiplier = Math.pow(10D, precision.doubleValue());
                double suggestedValue = (new Long(Math.round(num * multiplier))).doubleValue() / multiplier;
                if (suggestedValue == num) {
                    return null;
                } else {
                    String message = (new StringBuilder()).append("No more than ").append(precision.intValue()).append(
                        " digits after the decimal point.").toString();
                    return new ErrorMessage(getErrorString(validatorInfo, message), new Double(suggestedValue),value);
                }
            } else {
                return null;
            }
        }
    }
    
    static class floatLimit implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            double num;
            try {
                num = Double.valueOf(value.toString()).doubleValue();
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "Must be a valid decimal."));
            }
            Double min = validatorInfo.getMin();
            Double max = validatorInfo.getMax();
            if (max != null && num > max.doubleValue())
                return new ErrorMessage(getErrorString(validatorInfo), max);
            if (min != null && num < min.doubleValue())
                return new ErrorMessage(getErrorString(validatorInfo), min);
            Double precision = validatorInfo.getPrecision();
            if (precision != null) {
                double multiplier = Math.pow(10D, precision.doubleValue());
                double suggestedValue = (new Long(Math.round(num * multiplier))).doubleValue() / multiplier;
                if (suggestedValue == num) {
                    return null;
                } else {
                    String message = (new StringBuilder())
                        .append("No more than ")
                        .append(precision.intValue())
                        .append(" digits after the decimal point.")
                        .toString();
                    return new ErrorMessage(getErrorString(validatorInfo, message), new Double(suggestedValue),value);
                }
            } else {
                return null;
            }
        }
    }

    static class mask implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            String mask = validatorInfo.getMask();
            if (mask == null)
                throw new ValidationException( "mask validator called without valid mask");
            mask = (new StringBuilder()).append("/").append(mask).append("/").toString();
            if (Pattern.matches(mask, value.toString()))
                return new ErrorMessage(getErrorString(validatorInfo),null,value);
            else
                return null;
        }
    }

    static class substringCount implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            String substring = validatorInfo.getSubstring();
            String val = value.toString();
            if (substring == null)
                throw new ValidationException( "substringCount validator called without valid substring");
            long matchCount = 0L;
            int i = 0;
            do {
                if (i >= val.length())
                    break;
                i = val.indexOf(substring, i);
                if (i <= -1)
                    break;
                matchCount++;
                i++;
            } while (true);
            String operator = validatorInfo.getOperator();
            Long countObj = validatorInfo.getCount();
            if (operator == null)
                operator = "==";
            long count;
            if (countObj == null)
                count = 0L;
            else
                count = countObj.longValue();
            if (operator.equals("==")) {
                if (matchCount == count)
                    return null;
            } else if (operator.equals("!=")) {
                if (matchCount != count)
                    return null;
            } else if (operator.equals(">")) {
                if (matchCount > count)
                    return null;
            } else if (operator.equals("<")) {
                if (matchCount < count)
                    return null;
            } else if (operator.equals(">=")) {
                if (matchCount >= count)
                    return null;
            } else if (operator.equals("<=")) {
                if (matchCount <= count)
                    return null;
            } else {
                throw new ValidationException( (new StringBuilder()).append(
                    "in substringCount validator, operator was ").append(operator).append(", must be one of ==, !=, >, <, >= or <=").toString());
            }
            return new ErrorMessage(
                getErrorString(
                    validatorInfo,
                    (new StringBuilder()).append("Must contain ").append(operator).append(" ").append(count).append(" instances of '").append(
                        substring).append("'").toString()),null,value);
        }
    }
    
    static class doesntContain implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            String substring = validatorInfo.getSubstring();
            if (substring == null)
                throw new ValidationException( "doesntContain validator called without valid substring");
            if (value.toString().indexOf(substring) > -1)
                return new ErrorMessage(getErrorString(validatorInfo,
                    (new StringBuilder()).append("Must not contain '").append(substring).append("'").toString()),null,value);
            else
                return null;
        }
    }
    
    static class contains implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            String substring = validatorInfo.getSubstring();
            if (substring == null)
                throw new ValidationException("contains validator called without valid substring");
            if (value.toString().indexOf(substring) == -1)
                return new ErrorMessage(getErrorString(validatorInfo,
                    (new StringBuilder()).append("Must contain '").append(substring).append("'").toString()),null,value);
            else
                return null;
        }
    }

    
    static class matchesField implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo, Object value, String fieldName, Map<String, Object> record,
            ValidationContext context) throws ValidationException {
            Object otherField = validatorInfo.getProperty("otherField");
            if (otherField == null)
                throw new ValidationException("matchesField validator called without valid otherField");
            Object otherFieldValue = record.get(otherField);
            if (value == null && otherFieldValue == null || value.equals(otherFieldValue))
                return null;
            else
                return new ErrorMessage(getErrorString(validatorInfo,
                    (new StringBuilder()).append("Does not match value in field '").append(otherField).append("'").toString()),null,value);
        }
    }

    static class lengthRange implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo, Object value, String fieldName, Map<String, Object> record,
            ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            Double min = validatorInfo.getMin();
            Double max = validatorInfo.getMax();
            if (min == null && max == null)
                throw new ValidationException("lengthRange validator called without valid min or max");
            String s = value.toString();
            if (max != null && s.length() > max.longValue() || min != null && s.length() < min.longValue())
                return new ErrorMessage(getErrorString(validatorInfo,
                    (new StringBuilder()).append("Must be between ").append(min).append("-").append(max).append(" characters long").toString()),null,value);
            else
                return null;
        }
    }

    static class regex implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo, Object value, String fieldName, Map<String, Object> record,
            ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            String expression = validatorInfo.getExpression();
            if (expression == null)
                throw new ValidationException("regex validator called without expression");
            expression = (new StringBuilder()).append("/").append(expression).append("/").toString();
            if (Pattern.matches(expression, value.toString()))
                return new ErrorMessage(getErrorString(validatorInfo),null,value);
            else
                return null;
        }
    }
    
    static class integerRange implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            long num;
            try {
                num = Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notAnInteger"));
            }
            Double min = validatorInfo.getMin();
            Double max = validatorInfo.getMax();
            if (min == null && max == null)
                throw new ValidationException( "integerRange validator called without valid min or max");
            if (max != null && num > max.longValue())
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_mustBeShorterThan"), max, max);
            if (min != null && num < min.longValue())
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_mustBeLongerThan"), min, min);
            else
                return null;
        }
    }
    
    static class isUnique implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            Map<String,Object> params = context.getTemplateContext();
            DataService ds = (DataService) params.get(ValidationContext.TEMPLATE_DATASERVICE);
            FieldInfo field = (FieldInfo) params.get(ValidationContext.TEMPLATE_FIELD);
            if (field == null) {
                log.warn((new StringBuilder()).append("Field ")
                    .append(fieldName)
                    .append(" - 'isUnique' validation encountered a ")
                    .append( "template context where the field was not set.  Unable to  ")
                    .append("proceed, assuming false").toString());
                return new ErrorMessage(getErrorString(validatorInfo, "Value must be unique"));
            }
            String realFieldName = field.getName();
            if (ds == null) {
                log.warn((new StringBuilder()).append("Field ")
                    .append(fieldName)
                    .append(" - 'isUnique' validation encountered a ")
                    .append("template context where the dataService was not set.  Unable to  ")
                    .append("proceed, assuming false").toString());
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_mustBeUnique"));
            }
            try {
                if (ds.hasRecord(realFieldName, value))
                    return new ErrorMessage(getErrorString(validatorInfo, "%validator_mustBeUnique"),null,value);
            } catch (Exception e) {
                return new ErrorMessage(e.getMessage());
            }
            return null;
        }
    }
    static class isRegexp implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            try {
                java.util.regex.Pattern.compile(value.toString());
            } catch (Exception e) {
                return new ErrorMessage(getErrorString(validatorInfo, e.getMessage()),null,value);
            }
            return null;
        }
    }
    static class isString implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals("")) {
                return null;
            } else {
                context.setResultingValue(value.toString());
                return null;
            }
        }
    }
    
    static class isURL implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            try {
               URL url= new URL(value.toString());
               context.setResultingValue(url);
            } catch (MalformedURLException e) {
                return new ErrorMessage(getErrorString(validatorInfo, e.getMessage()),null,value);
            }
            return null;
        }
    }
    
    static class isIdentifier implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            if (DataUtils.isIdentifier((String) value))
                return null;
            else
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notAnIdentifier"),null,value);
        }
    }
    static class isFloat implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            try {
                double num = DataUtils.asDouble(value);
                context.setResultingValue(new Double(num));
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notADecimal"),null,value);
            }
            return null; 
        }
    }
    static class isTime implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || (value instanceof Date))
                return null;
            if (value.equals("")) {
                context.setResultingValue(null);
                return null;
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss Z");
            try {
                Date time = timeFormat.parse((new StringBuilder()).append(value.toString()).append(" -0000").toString());
                context.setResultingValue(time);
                return null;
            } catch (ParseException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notATime"),null,value);
            }
        }
    }
    static class isDate implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || (value instanceof Date))
                return null;
            if (value.equals("")) {
                context.setResultingValue(null);
                return null;
            }
            String dateString = value.toString();
            //自己配置的
            Object parttern=validatorInfo.getProperty(ValidationContext.DATE_FORMAT);
            //Field配置的 
            if(parttern==null){
               FieldInfo field =(FieldInfo)context.getTemplateContext().get(ValidationContext.TEMPLATE_FIELD);
               parttern=field.getDateFormat();
            }
            //自动选择的
            if(parttern==null){
                if (dateString.length() == 19) {
                    if (dateString.charAt(10) == 'T'){
                        parttern="yyyy-MM-dd'T'HH:mm:ss";
                    }else{
                        parttern="yyyy-MM-dd HH:mm:ss";
                    }
                }else{
                    parttern="yyyy-MM-dd";
                }
            }
            SimpleDateFormat dateFormat=new SimpleDateFormat(parttern.toString());;
            try {
                Date date = dateFormat.parse(dateString);
                context.setResultingValue(date);
                return null;
            } catch (ParseException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notADate"),null,value);
            }
        }
    }
    
    static class isInteger implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals("") || (value instanceof Integer))
                return null;
            long longValue;
            try {
                longValue = Long.parseLong(value.toString());
                context.setResultingValue(new Long(longValue));
            } catch (NumberFormatException e) {
                return new ErrorMessage(getErrorString(validatorInfo, "%validator_notAnInteger"),null,value);
            }
            return null;
        }
    }
    static class isBoolean implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext context) throws ValidationException {
            if (value == null || value.equals("") || (value instanceof Boolean))
                return null;
            if (value instanceof Number) {
                double number = ((Number) value).doubleValue();
                context.setResultingValue(new Boolean(number != 0.0D));
            } else{
              Boolean b=  DataUtils.asBooleanObject(value);
                if(b!=null){
                    context.setResultingValue(new Boolean((String) value));
                }else{
                    return new ErrorMessage(getErrorString(validatorInfo, "%validator_notABoolean"),null,value);
                }
                
            }
            return null;
        }
    }
    
    
    static class isOneOf implements Validator
    {
        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext validationcontext)
            throws ValidationException {
            if (value == null || value.equals(""))
                return null;
            if(validatorInfo.getProperty("valueMapList")!=null){
                boolean found = false;
                @SuppressWarnings("unchecked")
                Map<String,String> valueMap= (Map<String,String>)validatorInfo.getProperty("valueMapList");
                StringBuffer sb =new StringBuffer();
                for(String key:valueMap.keySet()){
                    sb.append(valueMap.get(key)).append(",");
                    if(value.toString().equals(key)){
                        found=true;
                        break;
                    }
                }
                if(!found){
                    return  new ErrorMessage(getErrorString(validatorInfo, "%validator_notOneOf"),null,sb.toString());
                }
                return null;
            }else{
                throw new ValidationException("isOneOf validator called without valid list for field: " + fieldName);
            }
        }
    }
    static class required implements Validator
    {

        @Override
        public ErrorMessage validate(ValidatorInfo validatorInfo,Object value, String fieldName, Map<String, Object> record, ValidationContext validationcontext)
            throws ValidationException {
            if (value == null || value.equals(""))
                return new ErrorMessage("%validator_requiredField");
            else
                return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.validation.ValidatorService#validateField(java.util.Map, java.lang.String, java.util.List, org.solmix.datax.validation.ValidationContext, java.lang.Object)
     */
    @Override
    public ErrorReport validateField(Map<String, Object> currentRecord, String fieldName, List<ValidatorInfo> validators, ValidationContext context,
        Object value,ErrorReport errorReport) {
        if (validators == null || validators.isEmpty())
            return null;
        context.clearResultingValue();
        
        // boolean stopIfFalse = false;
        String type = null;
        ErrorMessage error = null;
        for(ValidatorInfo vi :validators){
            type = vi.getType();
            Object rawCondition = vi.getProperty("applyWhen");
            if(rawCondition!=null){
                //XXX 额外条件判断
            }
            if(vi.getClientOnly()!=null&&vi.getClientOnly()){
                continue;
            }
            if (type == null)
                error= new ErrorMessage((new StringBuilder())
                    .append("Validator missing type property: ")
                    .append(type)
                    .append("\nIf this is a custom validator, set the clientOnly property to true.")
                    .toString());
            if (clientOnlyValidators.contains(type)){
                continue;
            }
            
            Validator vfunc = getBuiltinValidator(type);
            if (vfunc != null) {
                error = vfunc.validate(vi,value, fieldName, currentRecord, context);
                if (error != null) {
                    try {
                        // XXX Template process message.
                        // validator.evaluateErrorMessage(error);
                    } catch (Exception e) {
                        // throw new ValidatorException(e.getMessage());
                    }
                    error = context.localizedErrorMessage(error);
                }
            }
            
            if (errorReport == null)
                errorReport = new ErrorReport();
            if (error != null)
                errorReport.addError(fieldName, error);
        }
        return errorReport;
    }

    public  static String getErrorString(ValidatorInfo validatorInfo, String defaultString) {
        String error = validatorInfo.getErrorMessage();
        if(error==null){
            error=defaultString;
        }
        return error;
    }
    public static String getErrorString(ValidatorInfo params) {
        return getErrorString(params, "%validator_failed");
    }
    protected static Validator getBuiltinValidator(String validatorName) {
        return defaultValidators.get(validatorName);
    }
    
    
    public ValidatorInfo getValidatorInfo(FieldType type) {
        return buildInValidator.get(type);
    }
}
