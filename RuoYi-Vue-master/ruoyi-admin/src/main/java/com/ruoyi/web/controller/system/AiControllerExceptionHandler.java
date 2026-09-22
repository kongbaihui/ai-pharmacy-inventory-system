package com.ruoyi.web.controller.system;

import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ai.AiErrorCode;

/**
 * AI 接口的稳定错误协议，不暴露内部异常细节。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {
        AiChatController.class, AiKnowledgeController.class, AiInventoryReportController.class })
public class AiControllerExceptionHandler
{
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class,
            ConstraintViolationException.class })
    public AjaxResult handleValidation(Exception exception)
    {
        String message = validationMessage(exception);
        return error(AiErrorCode.PARAMETER_ERROR, message);
    }

    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleService(ServiceException exception)
    {
        AiErrorCode code = AiErrorCode.fromCode(exception.getCode());
        if (code == null)
        {
            code = AiErrorCode.MODEL_UNAVAILABLE;
        }
        return error(code, exception.getMessage());
    }

    private AjaxResult error(AiErrorCode code, String message)
    {
        AjaxResult result = AjaxResult.error(code.getCode(), message == null ? code.getMessage() : message);
        result.put("errorCode", code.getKey());
        return result;
    }

    private String validationMessage(Exception exception)
    {
        if (exception instanceof MethodArgumentNotValidException value
                && value.getBindingResult().getFieldError() != null)
        {
            return value.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof BindException value && value.getBindingResult().getFieldError() != null)
        {
            return value.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (exception instanceof ConstraintViolationException value && !value.getConstraintViolations().isEmpty())
        {
            return value.getConstraintViolations().iterator().next().getMessage();
        }
        return AiErrorCode.PARAMETER_ERROR.getMessage();
    }
}
