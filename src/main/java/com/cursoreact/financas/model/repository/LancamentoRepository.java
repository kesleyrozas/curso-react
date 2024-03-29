package com.cursoreact.financas.model.repository;

import com.cursoreact.financas.model.entity.Lancamento;
import com.cursoreact.financas.model.enums.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    @Query(value = " select sum(l.valor) from Lancamento l join l.usuario u " +
                   " where u.id = :idUsuario and l.tipo = :tipo group by u " )
    BigDecimal obterSaldoPorTipoLancamentoEUsuario(@Param("idUsuario") Long idUsuario, @Param("tipo") TipoLancamento tipo);
}
