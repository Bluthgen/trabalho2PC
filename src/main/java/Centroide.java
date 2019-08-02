import mpi.MPI;

class Centroide extends Elemento {

    int id;

    Centroide(int numD, int[] attr, int id) {
        super(numD, attr);
        this.id = id;
    }

    private boolean setAtributo(int i, int attr) {
        boolean mudado = atributos[i] != attr;
        atributos[i] = attr;
        return mudado;
    }

    boolean recalculaAtributos() {
        int num, soma, i;
        boolean mudado = false;
        for (i = 0; i < numDimensoes; i++) {
            soma = 0;
            num = 0;
            for (Elemento elemento : trabalho2PC.elementos) {
                if (elemento.getAssociado() == this.id) {
                    num++;
                    soma += elemento.getAtributo(i);
                }
            }
            if (num > 0) {
                if (setAtributo(i, soma / num)) {
                    mudado = true;
                }
            }
        }
        return mudado;
    }

    boolean recalculaAtributosPar(){
        double tamanhoFake = trabalho2PC.tamT-1;
        int[] numPorThread = new int[(int)trabalho2PC.tamT*2];
        numPorThread[0] = 0;
        numPorThread[1] = 0;
        numPorThread[2] = 0;
        numPorThread[3] = (int) Math.floor(trabalho2PC.tamElementos / (tamanhoFake*1.0));
        for(int i=4;i<trabalho2PC.tamT*2;i+=2){
            numPorThread[i]=numPorThread[i-1];
            if(i!=trabalho2PC.tamT*2-2){
                numPorThread[i+1] = numPorThread[i]+(int) Math.floor(trabalho2PC.tamElementos / (tamanhoFake*1.0));
            }else{
                numPorThread[i+1] = trabalho2PC.tamElementos;
            }
        }

        int[] recebidos = new int[2];
        MPI.COMM_WORLD.Scatter(numPorThread, 0, 1, MPI.INT, recebidos, 0, 2, MPI.INT, 0);

        boolean[] mudado = {false};
        int[] listaMudancas = new int[numDimensoes*2+1];
        int conn = 0;
        for (int i = 0; i < numDimensoes; i++) {
            int soma = 0;
            int num = 0;
            if(trabalho2PC.nrThread==0){
                int[] numA = {0};
                int[] somaA = {0};
                for(int op=1; op<trabalho2PC.tamT; op++){
                    MPI.COMM_WORLD.Recv(numA, 0,1,MPI.INT,op,0);
                    MPI.COMM_WORLD.Recv(somaA, 0,1,MPI.INT,op,1);
                    soma+=somaA[0];
                    num+=numA[0];
                }
                if (num > 0) {
                    int aux = soma / num;
                    if (setAtributo(i, aux)) {
                        listaMudancas[conn] = i;
                        listaMudancas[conn+1] = aux;
                        conn+=2;
                        mudado[0] = true;
                    }
                }
                listaMudancas[conn] = -1;
            }else{
                for(int k = recebidos[0]; k < recebidos[1]; k++) {
                    if (trabalho2PC.elementos.get(k).getAssociado() == this.id) {
                        num++;
                        soma += trabalho2PC.elementos.get(k).getAtributo(i);
                    }
                }
                int[] numA = {num};
                int[] somaA = {soma};
                MPI.COMM_WORLD.Send(numA, 0,1,MPI.INT,0,0);
                MPI.COMM_WORLD.Send(somaA, 0,1,MPI.INT,0,1);
            }
        }
        MPI.COMM_WORLD.Bcast(mudado,0,1,MPI.BOOLEAN,0);
        MPI.COMM_WORLD.Bcast(listaMudancas,0,listaMudancas.length,MPI.INT,0);
        if(mudado[0] && trabalho2PC.nrThread!=0){
            for(int l=0; l < listaMudancas.length;l+=2){
                if(listaMudancas[l]==-1){
                    break;
                }
                atributos[listaMudancas[l]]=listaMudancas[l+1];
            }
        }
        return mudado[0];
    }
}