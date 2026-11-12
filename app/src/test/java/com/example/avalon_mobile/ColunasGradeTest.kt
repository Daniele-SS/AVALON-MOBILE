package com.example.avalon_mobile

import com.example.avalon_mobile.util.ColunasGrade
import org.junit.Assert.assertEquals
import org.junit.Test

class ColunasGradeTest {
    // Home: 4 colunas, espaço 10, padding horizontal 4 (valores em px = dp com densidade 1)
    private fun home(larguraTela: Float, palavra: Float) =
        ColunasGrade.escolher(larguraTela, palavra, paddingHorizontalItem = 4f, espaco = 10f, maximo = 4)

    @Test fun fontePadraoMantemQuatroColunas() {
        // 360 dp - 52 de margem = 308; card = (308-30)/4 = 69,5; palavra 58 + 8 = 66 -> cabe
        assertEquals(4, home(308f, 58f))
    }

    @Test fun fonteGrandeReduzParaTresColunas() {
        // palavra 58 * 1,3 = 75 + 8 = 83 > 69,5; com 3 colunas card = (308-20)/3 = 96 -> cabe
        assertEquals(3, home(308f, 75f))
    }

    @Test fun fonteMuitoGrandeReduzParaDuasColunas() {
        // palavra 100 + 8 = 108 > 96; com 2 colunas card = (308-10)/2 = 149 -> cabe
        assertEquals(2, home(308f, 100f))
    }

    @Test fun palavraEnormeCaiParaUmaColuna() {
        assertEquals(1, home(308f, 200f))
    }

    @Test fun nuncaPassaDoMaximo() {
        assertEquals(3, ColunasGrade.escolher(1000f, 10f, 6f, 12f, maximo = 3))
    }

    @Test fun telaMaisComTresColunas() {
        // Mais: 360 - 40 = 320; 3 colunas -> card (320-24)/3 = 98,6; "psicossociais" 80 + 12 = 92 -> cabe
        assertEquals(3, ColunasGrade.escolher(320f, 80f, 6f, 12f, 3))
        // com fonte maior (palavra 100 + 12 = 112 > 98,6) -> 2 colunas (card 154)
        assertEquals(2, ColunasGrade.escolher(320f, 100f, 6f, 12f, 3))
    }
}
