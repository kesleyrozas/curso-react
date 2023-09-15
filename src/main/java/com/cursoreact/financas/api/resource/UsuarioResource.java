package com.cursoreact.financas.api.resource;

import com.cursoreact.financas.api.dto.UsuarioDTO;
import com.cursoreact.financas.exception.ErroAutenticacao;
import com.cursoreact.financas.exception.RegraNegocioException;
import com.cursoreact.financas.model.entity.Usuario;
import com.cursoreact.financas.service.LancamentoService;
import com.cursoreact.financas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioResource {

    private UsuarioService usuarioService;
    @Autowired
    private LancamentoService lancamentoService;

    @Autowired
    public UsuarioResource(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity autenticar(@RequestBody UsuarioDTO dto){

        try {
            Usuario usuarioAutenticado = usuarioService.autenticar(dto.getEmail(), dto.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        }catch (ErroAutenticacao e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioDTO dto){
        Usuario usuario = Usuario.builder().nome(dto.getNome())
                .email(dto.getEmail()).senha(dto.getSenha()).build();
        try {
            Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
        }catch (RegraNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("{id}/saldo")
    public ResponseEntity obterSaldo(@PathVariable("id") Long id){
       Optional<Usuario> usuario = usuarioService.obterPorId(id);

       if (!usuario.isPresent()){
           return new ResponseEntity(HttpStatus.NOT_FOUND);
       }

       BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);

       return  ResponseEntity.ok(saldo);
    }

}
