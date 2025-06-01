package br.com.saveit.apresentacao;

import br.com.saveit.dominio.Despesas;
import br.com.saveit.dominio.DespesasFixas;
import br.com.saveit.dominio.Usuario;
import br.com.saveit.dto.TelaDespesasFixas;
import br.com.saveit.servico.ServicoDespesasFixas;
import br.com.saveit.servico.ServicoUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/despesas-fixas")
public class DespesasFixasController {

    @Autowired
    private ServicoDespesasFixas servicoDespesasFixas;

    @Autowired
    private ServicoUsuarios servicoUsuarios;

    @PostMapping("/cadastrar")
    public ResponseEntity<Despesas> cadastrarDespesaFixa(@RequestBody TelaDespesasFixas telaDespesasFixas) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String emailUsuarioLogado = authentication.getName();

            Usuario usuarioAutenticado = servicoUsuarios.buscarPorEmail(emailUsuarioLogado);

            if (usuarioAutenticado == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Despesas despesaCadastrada = servicoDespesasFixas.cadastrarDespesaFixa(telaDespesasFixas, usuarioAutenticado);
            return new ResponseEntity<>(despesaCadastrada, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        return ResponseEntity.ok(servicoDespesasFixas.listarCategoriasPredefinidas());
    }

    @GetMapping("/periodicidades")
    public ResponseEntity<List<String>> listarPeriodicidades() {
        return ResponseEntity.ok(servicoDespesasFixas.listarPeriodicidadesPredefinidas());
    }

    @PutMapping("/editar/{id}")
public ResponseEntity<Despesas> editarDespesaFixa(
        @PathVariable Long id,
        @RequestBody TelaDespesasFixas telaDespesasFixas) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailUsuarioLogado = authentication.getName();
        Usuario usuarioAutenticado = servicoUsuarios.buscarPorEmail(emailUsuarioLogado);

        if (usuarioAutenticado == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        DespesasFixas despesaEditada = servicoDespesasFixas.editarDespesaFixa(id, telaDespesasFixas, usuarioAutenticado);
        return ResponseEntity.ok(despesaEditada);

    } catch (IllegalArgumentException e) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (SecurityException e) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

@DeleteMapping("/excluir/{id}")
public ResponseEntity<Void> excluirDespesaFixa(@PathVariable Long id) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String emailUsuarioLogado = authentication.getName();
        Usuario usuarioAutenticado = servicoUsuarios.buscarPorEmail(emailUsuarioLogado);

        if (usuarioAutenticado == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        servicoDespesasFixas.excluirDespesaFixa(id, usuarioAutenticado);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    } catch (IllegalArgumentException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (SecurityException e) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

}
