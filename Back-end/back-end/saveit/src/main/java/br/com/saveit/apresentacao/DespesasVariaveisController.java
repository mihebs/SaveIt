package br.com.saveit.apresentacao;

import br.com.saveit.dominio.Despesas;
import br.com.saveit.dominio.Usuario;
import br.com.saveit.dto.TelaDespesasVariaveis;
import br.com.saveit.servico.ServicoDespesasVariaveis;
import br.com.saveit.servico.ServicoUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/despesas-variaveis")

public class DespesasVariaveisController {
    @Autowired
    private ServicoDespesasVariaveis servicoDespesasVariaveis;

    @Autowired
    private ServicoUsuarios servicoUsuarios;

    @PostMapping("/cadastrar")
    public ResponseEntity<Despesas> cadastrarDespesaVariavel(
            @RequestBody TelaDespesasVariaveis telaDespesasVariaveis) { // <-- REMOVER @RequestAttribute Usuario usuarioAutenticado AQUI
        try {
            // Obter o objeto de autenticação do contexto de segurança do Spring
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Verificar se o usuário está autenticado
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Obter o "username" (geralmente o email) do usuário logado
            String emailUsuarioLogado = authentication.getName();

            // Buscar o objeto Usuario completo no banco de dados
            Usuario usuarioAutenticado = servicoUsuarios.buscarPorEmail(emailUsuarioLogado);

            if (usuarioAutenticado == null) {
                // Isso pode ocorrer se o usuário foi autenticado, mas não está no DB por algum motivo
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Agora você tem o objeto Usuario completo para usar no serviço
            Despesas despesaCadastrada = servicoDespesasVariaveis.cadastrarDespesaVariavel(telaDespesasVariaveis, usuarioAutenticado);
            return new ResponseEntity<>(despesaCadastrada, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(); // Para depuração
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/periodicidades")
    public ResponseEntity<List<String>> listarPeriodicidades() {
        return ResponseEntity.ok(servicoDespesasVariaveis.listarPeriodicidadesPredefinidas());
    }

    @PutMapping("/editar/{id}")
public ResponseEntity<Despesas> editarDespesaVariavel(
        @PathVariable Long id,
        @RequestBody TelaDespesasVariaveis telaDespesasVariaveis) {
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

        Despesas despesaEditada = servicoDespesasVariaveis.editarDespesaVariavel(id, telaDespesasVariaveis, usuarioAutenticado);
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
public ResponseEntity<Void> excluirDespesaVariavel(@PathVariable Long id) {
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

        servicoDespesasVariaveis.excluirDespesaVariavel(id, usuarioAutenticado);
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

