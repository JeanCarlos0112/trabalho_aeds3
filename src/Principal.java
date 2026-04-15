import java.util.*;

import entidades.usuarios.VisaoUsuario;

public class Principal {
public static void main(String[] args) {
        System.out.println("===========================");
        System.out.println("  Menu Inicial - Grupo 12  ");
        System.out.println("===========================");

        try {
            // A Visão de Usuário já inicializa os controles e abre os arquivos
            VisaoUsuario visao = new VisaoUsuario();
            
            // Dá o play no loop do menu
            visao.menuUsuario();

        } catch (Exception e) {
            System.err.println("Erro crítico ao inicializar os arquivos do sistema:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Sistema encerrado. Até mais!");
    }
}
