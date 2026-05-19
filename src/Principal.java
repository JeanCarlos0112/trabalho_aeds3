import java.util.Scanner;
import entidades.usuarios.*;
import entidades.cursos.*;
import entidades.inscricoes.*;

public class Principal {
    public static void main(String[] args) {
        ArquivoUsuario arqUsuario = null;
        ArquivoCurso arqCurso = null;
        ArquivoCursoUsuario arqInscricao = null;
        Scanner console = new Scanner(System.in);

        try {
            arqUsuario   = new ArquivoUsuario();
            arqCurso     = new ArquivoCurso();
            arqInscricao = new ArquivoCursoUsuario();

            ControleUsuario   ctrlUsuario   = new ControleUsuario(arqUsuario, arqCurso, arqInscricao);
            ControleCurso     ctrlCurso     = new ControleCurso(arqCurso, arqUsuario, arqInscricao);
            ControleInscricao ctrlInscricao = new ControleInscricao(arqInscricao, arqCurso, arqUsuario);

            VisaoCurso      visaoCurso      = new VisaoCurso(ctrlCurso, ctrlInscricao, console);
            VisaoInscricao  visaoInscricao  = new VisaoInscricao(ctrlInscricao, console);
            VisaoUsuario    visaoUsuario    = new VisaoUsuario(ctrlUsuario, visaoCurso, visaoInscricao, console);

            // Wire de dependencia circular (VisaoCurso <-> VisaoInscricao).
            // Cada um precisa abrir telas do outro: VisaoCurso delega
            // "Gerenciar inscritos" para VisaoInscricao, e VisaoInscricao
            // delega "Buscar por codigo / Listar todos" para VisaoCurso.
            visaoCurso.setVisaoInscricao(visaoInscricao);
            visaoInscricao.setVisaoCurso(visaoCurso);

            visaoUsuario.menuUsuario();

        } catch (Exception e) {
            System.err.println("Erro critico ao inicializar os arquivos do sistema:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (arqInscricao != null) arqInscricao.close();
                if (arqCurso != null)     arqCurso.close();
                if (arqUsuario != null)   arqUsuario.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar arquivos: " + e.getMessage());
            }
            console.close();
        }

        System.out.println("\nSistema encerrado. Ate mais!");
    }
}
