package br.com.portfolio.tarefas.dto;

import br.com.portfolio.tarefas.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados que o cliente ENVIA para criar ou atualizar uma tarefa.
 *
 * Usar um DTO separado da entidade protege o banco: o cliente nao consegue
 * definir campos controlados pelo servidor (id, status inicial, criadaEm).
 * As anotacoes abaixo validam os dados antes de chegar na regra de negocio.
 */
public class TarefaRequest {

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(min = 3, max = 100, message = "O titulo deve ter entre 3 e 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
    private String descricao;

    @NotNull(message = "A prioridade e obrigatoria (BAIXA, MEDIA ou ALTA)")
    private Prioridade prioridade;

    private LocalDate prazo;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }
}
