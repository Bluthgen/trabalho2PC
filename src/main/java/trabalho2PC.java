import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import mpi.*;

public class trabalho2PC{

    class Thread extends java.lang.Thread {

        volatile List<Elemento> elementos;

        Thread(List<Elemento> elementos) {
            this.elementos = elementos;
        }

        @Override
        public void run() {
            while(!para) {
                for (Elemento elemento : elementos) {
                    elemento.encontraCentroide(trabalho2PC.centroides);
                }

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //private CyclicBarrier barrier = new CyclicBarrier(quantThreads+1, new miolo());
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

    class miolo implements Runnable {

        @Override
        public void run() {
            int numC = 0;
            for (Centroide centroide : centroides) {
                boolean resultado = false;
                try {
                    resultado = centroide.recalculaAtributosPar(elementos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!resultado) {
                    numC++;
                }
            }
            if (numC == 20) {
                para = true;
            }
        }
    }

    private void k_meansPar() throws InterruptedException, BrokenBarrierException {
        int i, soma = 0;
        //List<Thread> threads = new ArrayList<>();
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
            //threads.add(new Thread(elemetoParaThread.get(i)));
            //threads.get(i).start();
        }

        while (!para) {
            barrier.await();
        }

        //for (Thread thread : threads) {
        //    thread.join();
        //}
    }

    public static void main(String[] args) throws IOException, InterruptedException, BrokenBarrierException {
        String appArgs[] = MPI.Init(args);
        int eu= MPI.COMM_WORLD.Rank();
        int tamanho= MPI.COMM_WORLD.Size();
        long startTempo = System.currentTimeMillis();
        switch (appArgs[0]) {
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
        }

        if (appArgs[1].equals("1")) {
            try{
                quantThreads = Integer.parseInt(args[2]);
            }catch (Exception e){
                System.out.println("O valor passado para a quantidade de threads não é um número");
                System.exit(0);
            }
            System.out.println("Execução Paralela");
            trabalho2PC trabalho = new trabalho2PC();
            trabalho.k_meansPar();
        } else {
            System.out.println("Execução Sequencial");
            k_meansSeq();
        }
        int i = 0;
        for (Elemento elemento : elementos) {

            System.out.println("Id: " + i + "\t" + "Classe: " + centroides.indexOf(elemento.getAssociado()));
            i++;
        }
        System.out.println("Tempo decorrido: " + Long.toString(System.currentTimeMillis() - startTempo));
        MPI.Finalize();
    }
}