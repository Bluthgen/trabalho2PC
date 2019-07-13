import java.util.List;

class Elemento {
    int numDimensoes;
    int[] atributos;
    private int associado;

    Elemento(int numD, int[] attr) {
        numDimensoes = numD;
        atributos = attr.clone();
    }

    int getAtributo(int i) {
        return atributos[i];
    }

    int getAssociado() {
        return associado;
    }

    Centroide getCentroide(List<Centroide> lista){
        return lista.get(associado);
    }

    void setAssociado(int centroide){
        associado= centroide;
    }

    void encontraCentroide(List<Centroide> centroides) {
        Centroide maisProximo = centroides.get(0);
        Integer menorDist = Integer.MAX_VALUE;
        for (Centroide centroide : centroides) {
            Integer distancia;
            int soma = 0;
            for (int i = 0; i < numDimensoes; i++) {
                soma += Math.pow((atributos[i] - centroide.getAtributo(i)), 2);
            }
            distancia = (int) Math.sqrt(soma);
            if (distancia < menorDist) {
                menorDist = distancia;
                maisProximo = centroide;
            }
        }
        associado = maisProximo.id;
    }
}