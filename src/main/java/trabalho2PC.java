import mpi.MPI;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class trabalho2PC{

    private volatile boolean para = false;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private static List<Elemento> elementos = new ArrayList<>();
    private static List<Centroide> centroides = new ArrayList<>();
    static int quantThreads;
    static List<Integer> dimensoesParaThread = new ArrayList<>();
    private static int numDimensoes;

    private static int[] centro(int num, Scanner scanner) {
        String linha = scanner.nextLine();
        String[] pedacos = linha.split(",");
        int[] atributos = new int[num];
        int j = 0;
        for (String pedaco : pedacos) {
            atributos[j] = Integer.parseInt(pedaco);
            j++;
        }
        return atributos;
    }

    private static void carregaElementos(List<Elemento> lista, int num) throws IOException {
        String fileName = Paths.get("").toAbsolutePath().toString() + "\\res\\int_base_" + num + ".data";
        Path path = Paths.get(fileName);
        int i = 0;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                int[] atributos = centro(num, scanner);
                lista.add(i, new Elemento(num, atributos));
                i++;
            }
        }
    }

    private static void carregaCentroide(List<Centroide> lista, int num) throws IOException {
        String fileName = Paths.get("").toAbsolutePath().toString() + "\\res\\int_centroid_" + num + "_20.data";
        Path path = Paths.get(fileName);
        int i = 0;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                int[] atributos = centro(num, scanner);
                lista.add(i, new Centroide(num, atributos));
                i++;
            }
        }
    }

    static private void k_meansSeq() {
        boolean para = false;
        int numC;
        while (!para) {
            for (Elemento elemento : elementos) {
                elemento.encontraCentroide(centroides);
            }
            numC = 0;
            for (Centroide centroide : centroides) {
                boolean mudado = centroide.recalculaAtributos(elementos);
                if (!mudado) {
                    numC++;
                }
            }
            if (numC == 20) {
                para = true;
            }
        }
    }

    private void k_meansPar() throws InterruptedException{
        int i, soma = 0;
        List<Thread> threads = new ArrayList<>();
        List<ArrayList<Elemento>> elemetoParaThread = new ArrayList<>();

        for (i = 0; i < quantThreads; i++) {
            elemetoParaThread.add(new ArrayList<>());
        }

        for (i = 0; i < quantThreads-1; i++) {
            elemetoParaThread.get(i).addAll(elementos.subList(soma, soma + elementos.size() / quantThreads));
            soma += elementos.size() / quantThreads;
        }
        elemetoParaThread.get(quantThreads-1).addAll(elementos.subList(soma, elementos.size()));

        soma = 0;

        for (i = 0; i < trabalho2PC.quantThreads - 1; i++) {
            dimensoesParaThread.add(i, soma + numDimensoes / trabalho2PC.quantThreads);
            soma += numDimensoes / trabalho2PC.quantThreads;
        }
        dimensoesParaThread.add(trabalho2PC.quantThreads - 1, numDimensoes);


        for (i = 0; i < quantThreads; i++) {
            threads.add(new Thread(elemetoParaThread.get(i)));
            threads.get(i).start();
        }

        while (!para) {
            barrier.await();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        MPI.Init(args);
        //nrThread ou rank não sei o melhor da refactor pois coloquei em varios lugares
        int nrThread = MPI.COMM_WORLD.Rank();
        int quantThreads= MPI.COMM_WORLD.Size();
        long startTempo = System.currentTimeMillis();
        if(nrThread == 0) {
            switch (args[3]) {
                case "161":
                    carregaElementos(elementos, 161);
                    carregaCentroide(centroides, 161);
                    numDimensoes = 161;
                    break;

                case "256":
                    carregaElementos(elementos, 256);
                    carregaCentroide(centroides, 256);
                    numDimensoes = 256;
                    break;

                case "1380":
                    carregaElementos(elementos, 1380);
                    carregaCentroide(centroides, 1380);
                    numDimensoes = 1380;
                    break;

                case "1601":
                    carregaElementos(elementos, 1601);
                    carregaCentroide(centroides, 1601);
                    numDimensoes = 1601;
                    break;

                default:
                    carregaElementos(elementos, 59);
                    carregaCentroide(centroides, 59);
                    numDimensoes = 59;
                    break;
            }//System.out.println("FIM "+nrThread);
        }else{
            //System.out.println("Entrando "+nrThread);
            MPI.COMM_WORLD.Barrier();
            //System.out.println("Saindo "+nrThread);
        }

        if (quantThreads > 1) {
            trabalho2PC trabalho = null;
            if(nrThread == 0) {
                System.out.println("Execução Paralela");
                trabalho = new trabalho2PC();
            }else{
                MPI.COMM_WORLD.Barrier();
            }
            trabalho.k_meansPar();
        } else {
            System.out.println("Execução Sequencial");
            k_meansSeq();
        }

        if(nrThread == 0) {
            int i = 0;
            for (Elemento elemento : elementos) {

                System.out.println("Id: " + i + "\t" + "Classe: " + centroides.indexOf(elemento.getAssociado()));
                i++;
            }
            System.out.println("Tempo decorrido: " + (System.currentTimeMillis() - startTempo));
        }else{
            MPI.COMM_WORLD.Barrier();
        }
        MPI.Finalize();
    }
}