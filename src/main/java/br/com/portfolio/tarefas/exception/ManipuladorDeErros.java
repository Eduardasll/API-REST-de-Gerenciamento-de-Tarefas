package br.com.portfolio.tarefas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centraliza o tratamento de erros da API.
 *
 * Em vez de cada Controller tratar excecoes, este "conselheiro" global
 * intercepta os erros e devolve respostas HTTP claras em JSON.
 */
@RestControllerAdvice
public class ManipuladorDeErros {

    // Tarefa inexistente -> 404 Not Found
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> tarefaNaoEncontrada(TarefaNaoEncontradaException ex) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("mensagem", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    // Dados invalidos (falha nas anotacoes @NotBlank, @Size, etc.) -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> dadosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("mensagem", "Dados invalidos");
        corpo.put("erros", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    // JSON malformado ou enum invalido no corpo -> 400 Bad Request
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException ex) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("mensagem", "Requisicao invalida: verifique o JSON enviado");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }
}
