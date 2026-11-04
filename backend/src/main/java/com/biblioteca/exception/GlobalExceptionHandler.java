package com.biblioteca.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler({AccesoDenegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> accesoDenegado(RuntimeException ex, HttpServletRequest req) {
        String msg = ex instanceof AccesoDenegadoException ? ex.getMessage()
                : "No tiene permisos para realizar esta acción";
        return construir(HttpStatus.FORBIDDEN, msg, req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> credenciales(BadCredentialsException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> deshabilitado(DisabledException ex, HttpServletRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, "Su cuenta está inactiva. Comuníquese con la biblioteca", req, null);
    }

    /** Errores de @Valid: devuelve un mapa campo -> mensaje. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return construir(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", req, errores);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido en esta ruta", req, null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> formato(Exception ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "Formato de datos incorrecto", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos (dato duplicado o en uso)", req, null);
    }

    /** Dos usuarios modificaron el mismo registro a la vez (ej. el último ejemplar de un libro). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrencia(OptimisticLockingFailureException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT,
                "Otro usuario modificó este registro al mismo tiempo. Actualice la página e intente nuevamente", req, null);
    }

    /** Parámetro sort con un campo que no existe (ej. ?sort=campoInexistente). */
    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorResponse> parametrosConsulta(RuntimeException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, "Parámetros de búsqueda u ordenamiento no válidos", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor", req, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, HttpServletRequest req,
                                                    Map<String, String> errores) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, req.getRequestURI(), errores);
        return ResponseEntity.status(status).body(body);
    }
}
