public class Main {

    public static void main(String[] args) {
        try {
            BenchmarkRunner.runBenchmark();

            StatisticsCalculator.generateSummary(
                "results/resultados.csv",
                "results/resumo.csv"
            );

            ChartGenerator.generateAllCharts(
                "results/resumo.csv",
                "charts"
            );

            System.out.println();
            System.out.println(
                "Todos os testes foram concluídos."
            );

        } catch (Exception exception) {
            System.err.println(
                "Erro durante a execução: "
                    + exception.getMessage()
            );

            exception.printStackTrace();
        }
    }
}