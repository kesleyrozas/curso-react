package com.cursoreact.financas.api.resource;

import com.cursoreact.financas.api.dto.AtualizaStatusDTO;
import com.cursoreact.financas.api.dto.LancamentoDTO;
import com.cursoreact.financas.exception.RegraNegocioException;
import com.cursoreact.financas.model.entity.Lancamento;
import com.cursoreact.financas.model.entity.Usuario;
import com.cursoreact.financas.model.enums.StatusLancamento;
import com.cursoreact.financas.model.enums.TipoLancamento;
import com.cursoreact.financas.service.LancamentoService;
import com.cursoreact.financas.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {

    private LancamentoService lancamentoService;
    private UsuarioService usuarioService;

    public LancamentoResource(LancamentoService lancamentoService, UsuarioService usuarioService) {
        this.lancamentoService = lancamentoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody LancamentoDTO dto){
        try {
            Lancamento entidade = converter(dto);
            entidade = lancamentoService.salvar(entidade);
            return new ResponseEntity(entidade, HttpStatus.CREATED);
        }catch (RegraNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto ){

        return lancamentoService.obterPorId(id).map(entity ->{
            try {
                Lancamento lancamento = converter(dto);
                lancamento.setId(entity.getId());
                lancamentoService.atualizar(lancamento);
                return ResponseEntity.ok(lancamento);
            }catch (RegraNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet( () -> new ResponseEntity("Lancamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));

    }

    @PutMapping("{id}/atualiza-status")
    public ResponseEntity atualizarStatus( @PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto){
        return lancamentoService.obterPorId(id).map( entity -> {
            StatusLancamento statusLancamento = StatusLancamento.valueOf(dto.getStatus());

            if (statusLancamento == null){
                return ResponseEntity.badRequest().body("Não foi possivél atualizar o status do lançamento");
            }

            try {
                entity.setStatus(statusLancamento);
                lancamentoService.atualizar(entity);
                return ResponseEntity.ok(entity);
            }catch (RegraNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }

        }).orElseGet (() -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
    }

    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam("usuario") Long idUsuario
            ){

        Lancamento lancamento = new Lancamento();
        lancamento.setDescricao(descricao);
        lancamento.setMes(mes);
        lancamento.setAno(ano);

        Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
        if(!usuario.isPresent()){
            return ResponseEntity.badRequest().body("Não foi possível realizar a consutla. Usuário não encontrado.");
        }else {
            lancamento.setUsuario(usuario.get());
        }

        List<Lancamento> lancamentos = lancamentoService.buscar(lancamento);
        return ResponseEntity.ok(lancamentos);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id){
        return lancamentoService.obterPorId(id).map(entidade ->{
            lancamentoService.deletar(entidade);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }).orElseGet( () -> new ResponseEntity("Lancamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST));

    }

    @GetMapping("{id}")
    public ResponseEntity obterLancamento(@PathVariable("id") Long id){
        return lancamentoService.obterPorId(id)
                                .map(lancamento -> new ResponseEntity(converter(lancamento), HttpStatus.OK))
                                .orElseGet(() -> new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    private LancamentoDTO converter(Lancamento lancamento){
        return LancamentoDTO.builder()
                .id(lancamento.getId())
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .status(lancamento.getStatus().name())
                .tipo(lancamento.getTipo().name())
                .usuario(lancamento.getUsuario().getId())
                .build();
    }

    private Lancamento converter(LancamentoDTO dto){
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setValor(dto.getValor());

        Usuario usuario = usuarioService
                .obterPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuario não encontrado para o ID informado"));
        lancamento.setUsuario(usuario);

        if(dto.getTipo() != null && !dto.getTipo().isEmpty()) {
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        }
        if(dto.getStatus() != null) {
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        }

        return lancamento;
    }

}
