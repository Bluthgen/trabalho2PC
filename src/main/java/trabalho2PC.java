import mpi.MPI;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class trabalho2PC {

    private static boolean para = false;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    static List<Elemento> elementos = new ArrayList<>();
    private static List<Centroide> centroides = new ArrayList<>();
    static int tamElementos;
    static double tamT;
    static int nrThread;

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
                lista.add(i, new Centroide(num, atributos, i));
                i++;
            }
        }
    }

    private void k_meansSeq() {
        int numC;
        while (!para) {
            for (Elemento elemento : elementos) {
                elemento.encontraCentroide(centroides);
            }
            numC = 0;
            for (Centroide centroide : centroides) {
                boolean mudado = centroide.recalculaAtributos();
                if (!mudado) {
                    numC++;
                }
            }
            if (numC == 20) {
                para = true;
            }
        }
    }

    private static void k_meansPar() {
        int numPorThread = (int) Math.ceil(tamElementos / tamT);
        int i, j, numC;
        boolean mudado;
        int[] atualizados = new int[numPorThread];
        int[] novos = new int[tamElementos];
        while (!para) {
            //Encontra o centroide
            for (i = nrThread; i < tamElementos; i += tamT) {
                elementos.get(i).encontraCentroide(centroides);
            }

            //Cria uma lista com todos os elementos atualizados e distribui
            if (nrThread == 0) {
                for (i = nrThread; i < tamElementos; i += tamT) {
                    novos[i] = elementos.get(i).getAssociado();
                }
                for (i = 1; i < tamT; i++) {
                    MPI.COMM_WORLD.Recv(atualizados, 0, numPorThread, MPI.INT, i, 0);
                    for (j = i; j < tamElementos; j += tamT) {
                        if (j / tamT >= atualizados.length)
                            break;
                        novos[j] = atualizados[j / (int) tamT];
                    }
                }
            } else {
                for (i = nrThread; i < tamElementos; i += tamT) {
                    atualizados[i / (int) tamT] = elementos.get(i).getAssociado();
                }
                MPI.COMM_WORLD.Send(atualizados, 0, numPorThread, MPI.INT, 0, 0);
            }
            MPI.COMM_WORLD.Bcast(novos, 0, tamElementos, MPI.INT, 0);

            for (i = 0; i < tamElementos; i++) {
                elementos.get(i).setAssociado(novos[i]);
            }

            numC = 0;
            for (Centroide centroide : centroides) {
                mudado = centroide.recalculaAtributosPar();
                if (!mudado) {
                    numC++;
                }
            }


            if (numC == 20) {
                para = true;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MPI.Init(args);
        tamT = MPI.COMM_WORLD.Size();
        nrThread = MPI.COMM_WORLD.Rank();
        long startTempo = System.currentTimeMillis();

        switch (args[3]) {
            case "161":
                carregaElementos(elementos, 161);
                carregaCentroide(centroides, 161);
                break;

            case "256":
                carregaElementos(elementos, 256);
                carregaCentroide(centroides, 256);
                break;

            case "1380":
                carregaElementos(elementos, 1380);
                carregaCentroide(centroides, 1380);
                break;

            case "1601":
                carregaElementos(elementos, 1601);
                carregaCentroide(centroides, 1601);
                break;

            default:
                carregaElementos(elementos, 59);
                carregaCentroide(centroides, 59);
                break;
        }
        tamElementos = elementos.size();
        if (tamT > 1) {
            if (nrThread == 1) {
                System.out.println("Execução Paralela");
            }
            k_meansPar();
        } else {
            System.out.println("Execução Sequencial");
            trabalho2PC trabalho = new trabalho2PC();
            trabalho.k_meansSeq();
            System.out.println("Tempo decorrido: " + (System.currentTimeMillis() - startTempo));
        }

        if (nrThread == 1) {
            /*int i = 0;
            for (Elemento elemento : elementos) {

                System.out.println("Id: " + i + "\t" + "Classe: " + elemento.getAssociado());
                i++;
            }
            */
            System.out.println("Tempo decorrido: " + (System.currentTimeMillis() - startTempo));
        } else {
            MPI.COMM_WORLD.Barrier();
        }
        MPI.Finalize();
    }
}