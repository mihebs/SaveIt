package br.com.saveit;

import br.com.saveit.dominio.Categoria;
import br.com.saveit.dominio.Despesas;
import br.com.saveit.dominio.DespesasFixas;
import br.com.saveit.dominio.Usuario;
import br.com.saveit.dto.TelaDespesasFixas;
import br.com.saveit.repositorio.RepositorioCategoria;
import br.com.saveit.repositorio.RepositorioDespesas;

import br.com.saveit.servico.ServicoDespesasFixas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServicoDespesasFixasTest {

    @Mock
    private RepositorioDespesas repositorioDespesas;
    @Mock
    private RepositorioCategoria repositorioCategoria;

    @InjectMocks
    private ServicoDespesasFixas servicoDespesasFixas;

    private Usuario usuario;
    private TelaDespesasFixas telaDespesasFixas;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);

        telaDespesasFixas = new TelaDespesasFixas();
        telaDespesasFixas.setValor(new BigDecimal("100.00"));
        telaDespesasFixas.setCategoria("Alimentação");
        telaDespesasFixas.setPeriodicidade("Mensal");
    }

    @Test
    void cadastrarDespesaFixa_ComDadosValidos_DeveSalvarDespesaECategoria() {
        Categoria categoriaExistente = new Categoria();
        categoriaExistente.setId(1L);
        categoriaExistente.setNome("Alimentação");

        when(repositorioCategoria.findByNome("Alimentação")).thenReturn(Optional.of(categoriaExistente));
        when(repositorioDespesas.save(any(DespesasFixas.class))).thenReturn(new DespesasFixas());

        Despesas despesaCadastrada = servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);

        assertNotNull(despesaCadastrada);
        verify(repositorioCategoria, times(1)).findByNome("Alimentação");
        verify(repositorioCategoria, never()).save(any(Categoria.class));
        verify(repositorioDespesas, times(1)).save(any(DespesasFixas.class));
    }

    @Test
    void cadastrarDespesaFixa_ComNovaCategoria_DeveSalvarDespesaECategoria() {
        when(repositorioCategoria.findByNome("Nova Categoria")).thenReturn(Optional.empty());

        Categoria novaCategoria = new Categoria();
        novaCategoria.setId(2L);
        novaCategoria.setNome("Nova Categoria");

        when(repositorioCategoria.save(any(Categoria.class))).thenReturn(novaCategoria);
        telaDespesasFixas.setCategoria("Nova Categoria");

        when(repositorioDespesas.save(any(DespesasFixas.class))).thenReturn(new DespesasFixas());

        Despesas despesaCadastrada = servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);

        assertNotNull(despesaCadastrada);
        verify(repositorioCategoria, times(1)).findByNome("Nova Categoria");
        verify(repositorioCategoria, times(1)).save(any(Categoria.class));
        verify(repositorioDespesas, times(1)).save(any(DespesasFixas.class));
    }

    @Test
    void cadastrarDespesaFixa_ComValorZero_DeveLancarExcecao() {
        telaDespesasFixas.setValor(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);
        });

        assertEquals("O valor da despesa deve ser maior que zero.", exception.getMessage());
        verify(repositorioCategoria, never()).findByNome(anyString());
        verify(repositorioCategoria, never()).save(any(Categoria.class));
        verify(repositorioDespesas, never()).save(any(Despesas.class));
    }

    @Test
    void cadastrarDespesaFixa_ComValorNegativo_DeveLancarExcecao() {
        telaDespesasFixas.setValor(new BigDecimal("-10.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);
        });

        assertEquals("O valor da despesa deve ser maior que zero.", exception.getMessage());
        verify(repositorioCategoria, never()).findByNome(anyString());
        verify(repositorioCategoria, never()).save(any(Categoria.class));
        verify(repositorioDespesas, never()).save(any(Despesas.class));
    }

    @Test
    void cadastrarDespesaFixa_ComCategoriaInvalida_DeveLancarExcecao() {

        telaDespesasFixas.setCategoria(null);

        IllegalArgumentException exceptionComNulo = assertThrows(IllegalArgumentException.class, () -> {
            servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);
        });
        assertEquals("A categoria não pode ser vazia.", exceptionComNulo.getMessage());

        telaDespesasFixas.setCategoria("");

        IllegalArgumentException exceptionComVazio = assertThrows(IllegalArgumentException.class, () -> {
            servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);
        });
        assertEquals("A categoria não pode ser vazia.", exceptionComVazio.getMessage());
    }

    @Test
    void cadastrarDespesaFixa_ComPeriodicidadeInvalida_DeveLancarExcecao() {
        telaDespesasFixas.setPeriodicidade("Periodicidade Inválida");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuario);
        });

        assertEquals("Periodicidade inválida. As opções são: Diário, Mensal, Semanal, Anual", exception.getMessage());
        verify(repositorioCategoria, never()).findByNome(anyString());
        verify(repositorioCategoria, never()).save(any(Categoria.class));
        verify(repositorioDespesas, never()).save(any(Despesas.class));
    }

    @Test
    void listarCategoriasPredefinidas_DeveRetornarListaCorreta() {
        List<String> categorias = servicoDespesasFixas.listarCategoriasPredefinidas();

        assertEquals(
                new HashSet<>(List.of("Transporte", "Alimentação", "Saúde", "Moradia", "Outros")),
                new HashSet<>(categorias)
        );
    }

    @Test
    void listarPeriodicidadesPredefinidas_DeveRetornarListaCorreta() {
        List<String> periodicidades = servicoDespesasFixas.listarPeriodicidadesPredefinidas();

        assertEquals(List.of("Diário", "Mensal", "Semanal", "Anual"), periodicidades);
    }

}
