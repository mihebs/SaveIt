package br.com.saveit;

import br.com.saveit.dominio.MetaFinanceira;
import br.com.saveit.dominio.Usuario;
import br.com.saveit.dto.TelaCadastroMetaFinanceira;
import br.com.saveit.repositorio.RepositorioMetasFinanceiras;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ServicoMetasFinanceirasTest {

    @Mock
    private RepositorioMetasFinanceiras repositorioMetasFinanceiras;

    @InjectMocks
    private ServicoMetasFinanceiras servicoMetasFinanceiras;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void criarMetaFinanceira_validaDeveCriarComSucesso() {
        TelaCadastroMetaFinanceira dto = new TelaCadastroMetaFinanceira();
        dto.setNome("Viajar");
        dto.setValorTotal(new BigDecimal("12000"));
        dto.setPrazo(12);
        dto.setUnidadePrazo("meses");

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(repositorioMetasFinanceiras.save(any(MetaFinanceira.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        MetaFinanceira meta = servicoMetasFinanceiras.criarMetaFinanceira(dto, usuario);

        assertNotNull(meta);
        assertEquals(dto.getNome(), meta.getNome());
        assertEquals(dto.getValorTotal(), meta.getValorTotal());
        assertEquals(BigDecimal.ZERO, meta.getValorAcumulado());
        assertEquals(dto.getPrazo(), meta.getPrazo());
        assertEquals(dto.getUnidadePrazo(), meta.getUnidadePrazo());
        assertEquals(usuario, meta.getUsuario());
    }

    @Test
    public void criarMetaFinanceira_valorTotalNuloDeveLancarException() {
        TelaCadastroMetaFinanceira dto = new TelaCadastroMetaFinanceira();
        dto.setNome("Investir");
        dto.setValorTotal(null);  // valor inválido
        dto.setPrazo(6);
        dto.setUnidadePrazo("meses");

        Usuario usuario = new Usuario();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicoMetasFinanceiras.criarMetaFinanceira(dto, usuario);
        });

        // Ajuste a mensagem de acordo com o que seu método lança
        assertEquals("Valor total deve ser positivo", exception.getMessage());
    }

    @Test
    public void calcularAporteMensal_deveCalcularCorretamente() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorTotal(new BigDecimal("24000"));
        meta.setValorAcumulado(new BigDecimal("4000"));
        meta.setPrazo(12);
        meta.setUnidadePrazo("meses");

        BigDecimal aporte = servicoMetasFinanceiras.calcularAporteMensal(meta);

        // Ajuste esperado para 1666.67 (arredondamento HALF_UP)
        assertEquals(new BigDecimal("1666.67"), aporte.setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    public void calcularValorRestante_deveCalcularCorretamente() {
        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorTotal(new BigDecimal("10000"));
        meta.setValorAcumulado(new BigDecimal("3000"));

        BigDecimal restante = servicoMetasFinanceiras.calcularValorRestante(meta);

        assertEquals(new BigDecimal("7000"), restante);
    }
}
