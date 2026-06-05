# Análise comparativa de algoritmos com uso de paralelismo

### Grupo:
Alanis Aguiar Bitencourt - 2325059

Kelma De Santana Alves - 2217003

## Resumo

Este trabalho apresenta uma análise comparativa do desempenho de três métodos de contagem de palavras em arquivos de texto: execução serial em CPU, execução paralela em CPU e execução paralela em GPU.

A implementação foi desenvolvida em Java. Para a execução paralela em CPU foi utilizado um pool de threads, enquanto a execução na GPU foi realizada com OpenCL por meio da biblioteca JOCL 2.0.4.

Os testes foram realizados com três arquivos de tamanhos diferentes. Cada configuração foi executada cinco vezes, e os resultados foram registrados em arquivos CSV. Também foram calculadas estatísticas como média, mediana, mínimo, máximo, desvio padrão, speedup e eficiência.

---

## 1. Introdução

A computação paralela permite que diferentes partes de uma tarefa sejam executadas simultaneamente. Entretanto, o uso de paralelismo nem sempre reduz o tempo de execução, pois existem custos relacionados à criação de threads, sincronização, compilação de kernels e transferência de dados entre CPU e GPU.

Neste trabalho, foi implementado um algoritmo de contagem de palavras utilizando três abordagens:

* `SerialCPU`: percorre todas as palavras sequencialmente;
* `ParallelCPU`: divide o vetor de palavras entre diferentes threads;
* `ParallelGPU`: utiliza OpenCL para comparar as palavras em paralelo na GPU.

O objetivo foi verificar como cada método se comporta com entradas de tamanhos diferentes e com diferentes quantidades de threads.

---

## 2. Metodologia

### Preparação dos textos

Os arquivos de texto foram lidos integralmente pelo programa.

Todo o conteúdo foi convertido para letras minúsculas e separado em palavras utilizando espaços, pontuações e outros caracteres não alfanuméricos como delimitadores.

Dessa forma, palavras como:

```text
The
THE
the
```

foram consideradas iguais.

A palavra utilizada nos testes foi:

```text
the
```

### Arquivos utilizados

Foram utilizados os seguintes textos:

| Arquivo               |         Tamanho em bytes |        Total de palavras |
| --------------------- | -----------------------: | -----------------------: |
| Dracula-165307.txt    |                   890389 |                   166773 |
| MobyDick-217452.txt   |                  1276285 |                   222559 |
| DonQuixote-388208.txt |                  2225840 |                   386868 |

Os valores foram obtidos durante a leitura dos arquivos pelo programa.

### Repetições

Cada configuração foi executada cinco vezes.

Antes das medições válidas, foram realizadas execuções de aquecimento para reduzir o impacto da compilação JIT da JVM.

O tempo foi medido utilizando:

```java
System.nanoTime()
```

O resultado foi convertido para milissegundos.

### Configurações da CPU

A versão paralela em CPU foi executada com:

```text
1 thread
2 threads
4 threads
8 threads
```

### Estatísticas

Para cada configuração foram calculados:

* média;
* mediana;
* menor tempo;
* maior tempo;
* desvio padrão;
* speedup;
* eficiência.

O speedup foi calculado por:

```text
Speedup = tempo médio serial / tempo médio paralelo
```

A eficiência da CPU paralela foi calculada por:

```text
Eficiência = speedup / quantidade de threads
```

---

## 3. Implementações

### SerialCPU

A versão serial percorre o vetor de palavras do início ao fim.

Sempre que uma palavra é igual à palavra pesquisada, o contador é incrementado.

```java
for (String word : words) {
    if (word.equals(targetWord)) {
        occurrences++;
    }
}
```

Essa implementação foi utilizada como referência para validar as versões paralelas.

### ParallelCPU

A versão paralela utiliza um `ExecutorService` com um pool fixo de threads.

O vetor de palavras é dividido em blocos. Cada thread processa um intervalo e retorna uma contagem parcial.

Ao final, todas as contagens são somadas.

Principais classes utilizadas:

```text
ExecutorService
Executors
Callable
Future
```

### ParallelGPU

A versão paralela em GPU utiliza OpenCL por meio da biblioteca JOCL.

As palavras são convertidas para valores inteiros utilizando `hashCode()`. Esses valores são enviados para a GPU.

Cada unidade de processamento compara um hash com o hash da palavra pesquisada.

O kernel utilizado está localizado em:

```text
src/kernels/word_count.cl
```

O kernel retorna `1` quando encontra a palavra e `0` quando não encontra.

Depois da execução, os resultados são enviados novamente para o Java e somados.

---

## 4. Resultados

Os resultados completos estão disponíveis em:

```text
results/resultados.csv
results/resumo.csv
```

O arquivo `resultados.csv` contém os resultados individuais de cada repetição.

O arquivo `resumo.csv` contém os valores estatísticos agrupados.

---

### Comparação entre os métodos
![Comparação entre os métodos](charts/comparacao_metodos.png)

O gráfico compara o tempo médio das versões serial, paralela em CPU e paralela em GPU.

A versão GPU apresentou tempos consideravelmente maiores que as versões executadas na CPU.

Isso aconteceu porque o tempo da GPU inclui diversas operações adicionais:

* criação do contexto OpenCL;
* seleção do dispositivo;
* criação da fila de comandos;
* leitura e compilação do kernel;
* criação dos buffers;
* transferência dos dados para a GPU;
* execução do kernel;
* transferência dos resultados para a CPU;
* liberação dos recursos.

Para os tamanhos de entrada utilizados, essa sobrecarga foi maior que o tempo necessário para realizar a contagem diretamente na CPU.

Portanto, a execução na GPU não apresentou vantagem para esse tipo e volume de entrada.

### Impacto da quantidade de threads

![Impacto da quantidade de threads](charts/comparacao_threads.png)

O gráfico mostra que aumentar a quantidade de threads não produz uma melhoria constante.

Em diferentes textos, a melhor configuração ocorreu geralmente entre duas e quatro threads.

Com oito threads, alguns testes apresentaram aumento no tempo de execução.

Isso ocorre porque existe um custo para:

* criar e gerenciar tarefas;
* dividir o vetor;
* aguardar os resultados;
* realizar troca de contexto;
* combinar as contagens parciais;
* compartilhar os recursos do processador.

Quando a quantidade de threads ultrapassa o ponto adequado para o tamanho da entrada, a sobrecarga pode reduzir o desempenho.

### Speedup

![Speedup da CPU paralela](charts/speedup.png)

Valores de speedup maiores que `1` indicam que a versão paralela foi mais rápida que a serial.

Valores menores que `1` indicam que a versão paralela foi mais lenta.

Os resultados mostram que algumas configurações com duas ou quatro threads apresentaram ganho de desempenho.

Entretanto, o aumento para oito threads reduziu o speedup em alguns casos.

Isso demonstra que o desempenho paralelo depende do tamanho da entrada, da quantidade de threads e das características do hardware utilizado.

### Validação

As três implementações produziram a mesma quantidade de ocorrências para cada arquivo.

Exemplo para o texto Dracula:

```text
SerialCPU: 8104 ocorrências
ParallelCPU: 8104 ocorrências
ParallelGPU: 8104 ocorrências
```

Essa validação demonstra que as três implementações realizaram a mesma operação corretamente.

---

## 5. Conclusão

O trabalho permitiu comparar diferentes formas de execução de um algoritmo de contagem de palavras.

A versão serial apresentou bom desempenho para os arquivos utilizados devido à simplicidade da operação e ao baixo custo de execução.

A versão paralela em CPU apresentou ganhos em determinadas configurações, principalmente com duas ou quatro threads. Entretanto, aumentar excessivamente o número de threads não garantiu melhor desempenho.

A versão GPU apresentou tempos superiores porque a inicialização do OpenCL e a transferência dos dados criaram uma sobrecarga significativa.

Os resultados demonstram que o paralelismo não deve ser aplicado apenas com o objetivo de utilizar mais recursos computacionais. É necessário avaliar o tamanho da entrada, o custo da operação, o hardware disponível e a sobrecarga da solução paralela.

Para entradas maiores ou para operações matemáticas mais complexas, a GPU poderia apresentar melhores resultados.

---

## 6. Bibliotecas utilizadas

Este projeto utiliza duas bibliotecas externas:

### JOCL 2.0.4

A biblioteca JOCL é utilizada para permitir o uso de OpenCL em Java e executar o algoritmo paralelo na GPU.

### XChart 3.8.8

A biblioteca XChart é utilizada para gerar os gráficos com os resultados dos testes.

No projeto, a biblioteca é utilizada principalmente pela classe:

```text
src/ChartGenerator.java
```

### Localização das bibliotecas

Os dois arquivos `.jar` devem ser colocados dentro da pasta `libs`, localizada na raiz do projeto.

A estrutura deve ficar assim:

```text
trabalho-computacao-paralela-alanis/
├── libs/
│   ├── jocl-2.0.4.jar
│   └── xchart-3.8.8.jar
├── src/
├── amostras/
├── results/
└── charts/
```

O arquivo da biblioteca JOCL deve estar em:

```text
libs/jocl-2.0.4.jar
```

O arquivo da biblioteca XChart deve estar em:

```text
libs/xchart-3.8.8.jar
```

Caso os arquivos sejam colocados em outra pasta, os comandos de compilação e execução precisarão ser alterados para utilizar o novo caminho.

---

## Link do Repositório
https://github.com/alanisaguiar/An-lise-comparativa-de-algoritmos-com-uso-de-paralelismo-/tree/main
