package com.example.avalon_mobile

import com.example.avalon_mobile.util.Datas
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatasNotificacaoTest {
    private val sp = ZoneId.of("America/Sao_Paulo")
    private val agora = LocalDateTime.of(2026, 11, 16, 10, 0)

    @Test
    fun `iso com Z converte para o fuso do aparelho`() {
        // 12:12 UTC = 09:12 em Sao Paulo (UTC-3)
        assertEquals("Hoje, 09:12", Datas.dataHoraRotulo("2026-11-16T12:12:00.000Z", agora, sp))
    }

    @Test
    fun `ontem e data antiga`() {
        assertEquals("Ontem, 17:25", Datas.dataHoraRotulo("2026-11-15T20:25:00.000Z", agora, sp))
        assertEquals("13/11/2026, 14:05", Datas.dataHoraRotulo("2026-11-13T17:05:00.000Z", agora, sp))
    }

    @Test
    fun `sem fuso assume horario local`() {
        assertEquals("Hoje, 08:40", Datas.dataHoraRotulo("2026-11-16 08:40:00", agora, sp))
        assertEquals("Ontem, 00:05", Datas.dataHoraRotulo("2026-11-15T00:05:00", agora, sp))
    }

    @Test
    fun `virada de dia respeita o fuso`() {
        // 02:30 UTC de 16/11 = 23:30 de 15/11 em Sao Paulo
        assertEquals("Ontem, 23:30", Datas.dataHoraRotulo("2026-11-16T02:30:00.000Z", agora, sp))
    }

    @Test
    fun `texto invalido ou vazio devolve null`() {
        assertNull(Datas.dataHoraRotulo(null, agora, sp))
        assertNull(Datas.dataHoraRotulo("  ", agora, sp))
        assertNull(Datas.dataHoraRotulo("ontem", agora, sp))
    }
}
