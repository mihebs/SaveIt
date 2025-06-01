package br.com.saveit.servico;

import br.com.saveit.dominio.Categoria;
import br.com.saveit.dominio.Despesas;
import br.com.saveit.dominio.DespesasFixas;
import br.com.saveit.dominio.Usuario;
import br.com.saveit.dto.TelaDespesasFixas;
import br.com.saveit.repositorio.RepositorioCategoria;
import br.com.saveit.repositorio.RepositorioDespesas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ServicoDespesasFixas {
    @Autowired
    private RepositorioDespesas repositorioDespesas;
    @Autowired
    private RepositorioCategoria repositorioCategoria;

    private static final List<String> PERIODICIDADES_PREDEFINIDAS = Collections.unmodifiableList(Arrays.asList("Diário", "Mensal", "Semanal", "Anual"));

    private static final List<String> CATEGORIAS_PREDEFINIDAS = Collections.unmodifiableList(Arrays.asList("Outros", "Alimentação", "Moradia", "Transporte", "Saúde"// Adicionei algumas categorias como exemplo
    ));


    public Despesas cadastrarDespesaFixa(TelaDespesasFixas telaDespesasFixas, Usuario usuario) {

        if (telaDespesasFixas.getValor() == null || telaDespesasFixas.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        if (telaDespesasFixas.getPeriodicidade() == null || telaDespesasFixas.getPeriodicidade().trim().isEmpty() || !PERIODICIDADES_PREDEFINIDAS.contains(telaDespesasFixas.getPeriodicidade().trim())) {
            throw new IllegalArgumentException("Periodicidade inválida. As opções são: " + String.join(", ", PERIODICIDADES_PREDEFINIDAS));
        }
        if (telaDespesasFixas.getCategoria() == null || telaDespesasFixas.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("A categoria não pode ser vazia.");
        }

        Optional<Categoria> categoriaOptional = repositorioCategoria.findByNome(telaDespesasFixas.getCategoria().trim());
        Categoria categoria;

        if (categoriaOptional.isPresent()) {
            categoria = categoriaOptional.get();
        } else {
            categoria = new Categoria();
            categoria.setNome(telaDespesasFixas.getCategoria().trim());
            repositorioCategoria.save(categoria);
        }

        DespesasFixas despesas = new DespesasFixas();
        despesas.setValor(telaDespesasFixas.getValor());
        despesas.setPeriodicidade(telaDespesasFixas.getPeriodicidade());
        despesas.setCategoria(categoria);
        despesas.setUsuario(usuario);

        return repositorioDespesas.save(despesas);
    }

    public List<String> listarCategoriasPredefinidas() {
        return CATEGORIAS_PREDEFINIDAS;
    }

    public List<String> listarPeriodicidadesPredefinidas() {
        return PERIODICIDADES_PREDEFINIDAS;
    }

    public DespesasFixas editarDespesaFixa(Long id, TelaDespesasFixas dto, Usuario usuario) {
    DespesasFixas despesaExistente = (DespesasFixas) repositorioDespesas.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

    if (!despesaExistente.getUsuario().getId().equals(usuario.getId())) {
        throw new SecurityException("Usuário não autorizado a editar esta despesa");
    }

    if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
    }
    if (dto.getPeriodicidade() == null || dto.getPeriodicidade().trim().isEmpty() || !PERIODICIDADES_PREDEFINIDAS.contains(dto.getPeriodicidade().trim())) {
        throw new IllegalArgumentException("Periodicidade inválida. As opções são: " + String.join(", ", PERIODICIDADES_PREDEFINIDAS));
    }
    if (dto.getCategoria() == null || dto.getCategoria().trim().isEmpty()) {
        throw new IllegalArgumentException("A categoria não pode ser vazia.");
    }

    Optional<Categoria> categoriaOptional = repositorioCategoria.findByNome(dto.getCategoria().trim());
    Categoria categoria;

    if (categoriaOptional.isPresent()) {
        categoria = categoriaOptional.get();
    } else {
        categoria = new Categoria();
        categoria.setNome(dto.getCategoria().trim());
        repositorioCategoria.save(categoria);
    }

    despesaExistente.setValor(dto.getValor());
    despesaExistente.setPeriodicidade(dto.getPeriodicidade());
    despesaExistente.setCategoria(categoria);

    return repositorioDespesas.save(despesaExistente);
}

public void excluirDespesaFixa(Long id, Usuario usuario) {
    DespesasFixas despesaExistente = (DespesasFixas) repositorioDespesas.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada"));

    if (!despesaExistente.getUsuario().getId().equals(usuario.getId())) {
        throw new SecurityException("Usuário não autorizado a excluir esta despesa");
    }

    repositorioDespesas.deleteById(id);
}

    
}
