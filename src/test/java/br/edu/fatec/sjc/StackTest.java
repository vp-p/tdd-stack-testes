package br.edu.fatec.sjc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomStack")
public class StackTest {

    private static final int LIMIT = 3;

    @Mock
    private CalculableStrategy<Double> calculableStrategy;

    private CustomStack<Double> cut;

    @BeforeEach
    void setUp() {
        cut = new CustomStack<>(LIMIT, calculableStrategy);
    }

    /** Stub: a estratégia devolve o próprio valor recebido. */
    private void stubIdentity() {
        when(calculableStrategy.calculateValue(anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("quando a pilha acabou de ser criada")
    class QuandoNova {

        @Test
        @DisplayName("deve estar vazia e com tamanho zero")
        void deveEstarVazia() {
            assertAll(
                    () -> assertTrue(cut.isEmpty()),
                    () -> assertEquals(0, cut.size()));
            verifyNoInteractions(calculableStrategy);
        }

        @Test
        @DisplayName("pop deve lançar StackEmptyException")
        void popDeveLancarExcecao() {
            assertThrows(StackEmptyException.class, () -> cut.pop());
        }

        @Test
        @DisplayName("top deve lançar IndexOutOfBoundsException")
        void topDeveLancarExcecao() {
            assertThrows(IndexOutOfBoundsException.class, () -> cut.top());
        }
    }

    @Nested
    @DisplayName("push")
    class Push {

        @Test
        @DisplayName("deve armazenar o valor calculado pela estratégia")
        void deveArmazenarValorCalculado() throws Exception {
            when(calculableStrategy.calculateValue(2.0)).thenReturn(4.0);

            cut.push(2.0);

            assertAll(
                    () -> assertFalse(cut.isEmpty()),
                    () -> assertEquals(1, cut.size()),
                    () -> assertEquals(4.0, cut.top()));
            verify(calculableStrategy, times(1)).calculateValue(2.0);
        }

        @ParameterizedTest(name = "push({0}) com estratégia de dobro resulta em {1}")
        @CsvSource({
                "1.0, 2.0",
                "-3.5, -7.0",
                "0.0, 0.0",
                "10.25, 20.5"
        })
        @DisplayName("deve aplicar a estratégia (fake de dobro) a cada valor")
        void deveAplicarEstrategia(double entrada, double esperado) throws Exception {
            CalculableStrategy<Double> dobro = value -> value * 2;
            CustomStack<Double> stack = new CustomStack<>(LIMIT, dobro);

            stack.push(entrada);

            assertEquals(esperado, stack.top());
        }

        @ParameterizedTest(name = "limite {0}")
        @ValueSource(ints = {1, 2, 5, 10})
        @DisplayName("deve aceitar elementos até o limite e lançar StackFullException ao exceder")
        void deveRespeitarLimite(int limite) throws Exception {
            stubIdentity();
            CustomStack<Double> stack = new CustomStack<>(limite, calculableStrategy);

            for (int i = 0; i < limite; i++) {
                stack.push((double) i);
            }

            assertEquals(limite, stack.size());
            assertThrows(StackFullException.class, () -> stack.push(99.0));
            assertEquals(limite, stack.size());
            verify(calculableStrategy, never()).calculateValue(99.0);
        }

        @Test
        @DisplayName("limite zero: deve lançar StackFullException no primeiro push")
        void limiteZero() {
            CustomStack<Double> stack = new CustomStack<>(0, calculableStrategy);

            assertThrows(StackFullException.class, () -> stack.push(1.0));
            verifyNoInteractions(calculableStrategy);
        }

        @Test
        @DisplayName("deve propagar NullPointerException da estratégia sem empilhar")
        void devePropagarExcecaoDaEstrategia() {
            when(calculableStrategy.calculateValue(null)).thenThrow(NullPointerException.class);

            assertThrows(NullPointerException.class, () -> cut.push(null));
            assertTrue(cut.isEmpty());
            assertEquals(0, cut.size());
        }
    }

    @Nested
    @DisplayName("pop e top")
    class PopETop {

        @BeforeEach
        void preencher() throws Exception {
            stubIdentity();
            cut.push(1.0);
            cut.push(2.0);
            cut.push(3.0);
        }

        @Test
        @DisplayName("top deve retornar o último elemento sem removê-lo")
        void topNaoRemove() {
            assertEquals(3.0, cut.top());
            assertEquals(3.0, cut.top());
            assertEquals(3, cut.size());
        }

        @Test
        @DisplayName("pop deve remover os elementos em ordem LIFO até esvaziar")
        void popEmOrdemLifo() throws Exception {
            assertEquals(3.0, cut.pop());
            assertEquals(2.0, cut.top());
            assertEquals(2.0, cut.pop());
            assertEquals(1.0, cut.pop());

            assertTrue(cut.isEmpty());
            assertThrows(StackEmptyException.class, () -> cut.pop());
        }

        @Test
        @DisplayName("após pop deve ser possível empilhar novamente")
        void pushAposPop() throws Exception {
            assertThrows(StackFullException.class, () -> cut.push(4.0));

            assertEquals(3.0, cut.pop());
            cut.push(5.0);

            assertEquals(5.0, cut.top());
            assertEquals(3, cut.size());
            verify(calculableStrategy).calculateValue(5.0);
        }
    }
}
