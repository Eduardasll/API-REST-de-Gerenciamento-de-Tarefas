package br.com.portfolio.tarefas.controller;

import br.com.portfolio.tarefas.dto.AtualizarStatusRequest;
import br.com.portfolio.tarefas.dto.TarefaRequest;
import br.com.portfolio.tarefas.dto.TarefaResponse;
import br.com.portfolio.tarefas.model.StatusTarefa;
import br.com.portfolio.tarefas.model.Tarefa;
import br.com.portfolio.tarefas.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Camada que recebe as requisicoes HTTP e devolve as respostas.
 *
 * Cada metodo abaixo corresponde a uma rota (endpoint) da API.
 * O Controller nao contem regra de negocio: ele delega para o Service.
 */
@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    // POST /tarefas -> cria uma tarefa e retorna 201 Created
    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody TarefaRequest request) {
        Tarefa tarefa = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TarefaResponse.de(tarefa));
    }

    // GET /tarefas e GET /tarefas?status=PENDENTE -> lista (com filtro opcional)
    @GetMapping
    public List<TarefaResponse> listar(@RequestParam(required = false) StatusTarefa status) {
        return service.listar(status).stream()
                .map(TarefaResponse::de)
                .toList();
    }

    // GET /tarefas/{id} -> busca uma tarefa
    @GetMapping("/{id}")
    public TarefaResponse buscarPorId(@PathVariable Long id) {
        return TarefaResponse.de(service.buscarPorId(id));
    }

    // PUT /tarefas/{id} -> atualiza os dados de uma tarefa
    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TarefaRequest request) {
        return TarefaResponse.de(service.atualizar(id, request));
    }

    // PATCH /tarefas/{id}/status -> altera apenas o status
    @PatchMapping("/{id}/status")
    public TarefaResponse alterarStatus(@PathVariable Long id,
                                        @Valid @RequestBody AtualizarStatusRequest request) {
        return TarefaResponse.de(service.alterarStatus(id, request.getStatus()));
    }

    // DELETE /tarefas/{id} -> exclui e retorna 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
