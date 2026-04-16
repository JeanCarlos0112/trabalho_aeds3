# Guia de Estrutura e Integração — TP1 AEDs III

## Estrutura de Diretórios Recomendada

```
projeto/
├── src/
│   ├── aed3/                                  ← Pacote do professor (não alterar)
│   │   ├── Arquivo.java                       ← CRUD genérico
│   │   ├── ArvoreBMais.java                   ← Árvore B+
│   │   ├── HashExtensivel.java                ← Hash Extensível
│   │   ├── InterfaceArvoreBMais.java
│   │   ├── InterfaceEntidade.java
│   │   ├── InterfaceHashExtensivel.java
│   │   ├── ParIDEndereco.java                 ← Par (id, endereço) — índice direto
│   │   ├── ParIdId.java                       ← Par (id1, id2) — relacionamento 1:N
│   │   └── ParNomeId.java                     ← Par (nome, id) — índice por nome
│   │
│   ├── entidades/
│   │   ├── usuarios/
│   │   │   ├── Usuario.java                   ← Entidade (MIRO)
│   │   │   ├── ArquivoUsuario.java            ← CRUD + índice email (MIRO + JEAN)
│   │   │   ├── ParEmailId.java                ← Par (email, id) para hash (JEAN)
│   │   │   ├── VisaoUsuario.java              ← View — E/S de dados (LUIZ)
│   │   │   └── ControleUsuario.java           ← Controller — lógica + menu (LUIZ)
│   │   │
│   │   └── cursos/
│   │       ├── Curso.java                     ← Entidade (MIRO)
│   │       ├── ArquivoCurso.java              ← CRUD + B+ para 1:N (JEAN)
│   │       ├── VisaoCurso.java                ← View — E/S de dados (ANDRÉ)
│   │       └── ControleCurso.java             ← Controller — lógica + menu (ANDRÉ)
│   │
│   └── Principal.java                         ← Main (menu de login + menu principal)
│
└── dados/                                     ← Gerado automaticamente em runtime
    ├── usuarios/
    │   ├── dados.db
    │   ├── indiceDireto.d.db
    │   ├── indiceDireto.c.db
    │   ├── indiceEmail.d.db
    │   └── indiceEmail.c.db
    └── cursos/
        ├── dados.db
        ├── indiceDireto.d.db
        ├── indiceDireto.c.db
        ├── indiceUsuarioCurso.btree.db        ← Árvore B+ do relacionamento 1:N
        └── indiceNomeCurso.btree.db           ← Árvore B+ por nome (ordem alfabética)
```

## O que o eu (**JEAN**) entreguei (relacionamento 1:N)

### 1. `ParEmailId.java` → `entidades/usuarios/`

Implementação completa do par (email, id) para a Hash Extensível.
Tamanho fixo de 54 bytes (50 para email + 4 para id).
O `ArquivoUsuario` usa isso para buscar usuário pelo email no login.

### 2. `ArquivoCurso.java` → `entidades/cursos/`

Substitui o arquivo do Miro. Contém dois índices Árvore B+:

- **`indiceUsuarioCurso`** — `ParIdId(idUsuario, idCurso)` — É O RELACIONAMENTO 1:N.
- **`indiceNomeCurso`** — `ParNomeId(nomeCurso, idCurso)` — Para listar em ordem alfabética.

Métodos novos disponíveis para o André e o Luiz usarem:

| Método                             | O que faz                                               | Quem usa     |
|------------------------------------|---------------------------------------------------------|--------------|
| `readAll(int idUsuario)`           | Retorna ArrayList com todos os cursos do usuário        | André        |
| `readAllOrdenadoPorNome(int idUsr)`| Igual ao acima, mas em ordem alfabética                 | André        |
| `verificaUsuarioTemCursos(int id)` | Retorna true/false se o usuário tem cursos cadastrados  | Luiz         |

Os overrides de `create`, `delete` e `update` mantêm os índices sincronizados
automaticamente — o André e o Luiz não precisam se preocupar com isso.

### 3. `ArquivoUsuario.java` → `entidades/usuarios/`

Substitui o arquivo do Miro. Adicionei overrides de `delete` e `update` para
manter o índice de email sincronizado (o Miro só tinha `create` e `readEmail`).

---

## Como funciona o 1:N na prática

O `ParIdId` do professor tem um truque no `compareTo`:

```java
public int compareTo(ParIdId a) {
    if (this.id1 != a.id1)
        return this.id1 - a.id1;
    else {
        if (this.id2 == -1)   // ← CORINGA: -1 casa com qualquer id2
            return 0;
        else
            return this.id2 - a.id2;
    }
}
```

Então quando o André chamar `readAll(3)`, internamente acontece:

1. Cria `ParIdId(3, -1)` como chave de busca
2. A Árvore B+ percorre as folhas comparando
3. Como `id2 == -1`, o `compareTo` retorna 0 para **QUALQUER** par que tenha `id1 == 3`
4. Retorna todos: `(3,1)`, `(3,5)`, `(3,12)` — ou seja, cursos 1, 5 e 12 do usuário 3

---

## O que eu (LUIZ) entreguei (Interface e Controle de Usuário) 

### 1. ControleUsuario.java → entidades/usuarios/ 

Funciona como o "cérebro" do sistema de usuários. Fazendo a ponte entre a interface e o arquivo de dados. Implementa as regras as seguintes regras: 
- Validação de Email — Garante que dois usuários não tenham o mesmo email no cadastro ou na atualização. 
- Segurança — Gerencia o login e a recuperação de senha comparando hashes, garantindo que a senha real nunca seja manipulada em texto aberto. 
- Lógica de Exclusão — Implementa a trava de segurança; se o usuário tiver cursos ativos, a exclusão é abortada. 

### 2. VisaoUsuario.java → entidades/usuarios/ 

Sistema de menus dinâmico que gerencia o estado da sessão (usuário logado e deslogado). 
- Menu Deslogado — Cadastro, Login e Recuperação de Senha. 
- Menu Logado — Visualização, alteração de dados e exclusão de conta. 
- UX de Recuperação — Diferente de um CRUD comum, a visão primeiro busca a pergunta secreta do usuário para só após a resposta correta do usuário para depois permitir a alteração da senha.

| Método                             | O que faz                                               |
|------------------------------------|---------------------------------------------------------|
| `cadastrarUsuario` | Valida email único e cria nova entidade.         |
| `recuperarSenha `| Valida resposta secreta e sobrescreve o hash da senha.             |
| `excluirUsuario ` | Executa a limpeza em cascata (remove cursos inativos) após validação.  |
| `obterPerguntaSecreta ` | Busca apenas a string da pergunta antes da validação da resposta.   |

##Como funciona a Exclusão Segura na prática 

```java
public boolean excluirUsuario(int idUsuario) throws Exception {
    // 1. Bloqueia se houver cursos ativos
    if (arqCurso.verificaUsuarioTemCursos(idUsuario)) {
        return false;   // A exclusão não é executada e retorna false 
    }

    // 2. Remove todos os cursos inativos associados  ao usuário
    ArrayList<Curso> cursosDoUsuario = arqCurso.readAll(idUsuario);
    for (Curso c : cursosDoUsuario) {
        if (c.getEstado() == 2 || c.getEstado() == 3) {
            arqCurso.delete(c.getID());
        }
    }

    // 3. Remove o usuário
    return arqUsuario.delete(idUsuario);
}
```

A lógica que implementei no excluirUsuario garante a integridade do banco de dados evitando que um usuário "suma" deixando cursos ativos pendentes. O fluxo funciona assim: 
1. Primeiro, chamo o método do Jean arqCurso.verificaUsuarioTemCursos(id). Se ele retornar true, eu interrompo tudo e aviso o usuário que ele tem pendências. 
2. Se não houver cursos ativos, o código recupera todos os cursos vinculados àquele ID via readAll(idUsuario). 
3. Eu percorro a lista e deleto apenas os cursos que estão nos estados 2 (Concluído) ou 3 (Cancelado). 
4. Ao final, o arqUsuario.delete aciona o índice do Jean, removendo o email da Hash Extensível automaticamente.


## Notas para os outros membros

### Para o ANDRÉ (ControleCurso):

- Use `arqCurso.readAllOrdenadoPorNome(idUsuarioAtivo)` para montar o menu de cursos.
- Use `arqCurso.create(curso)` normalmente — o índice 1:N é mantido automaticamente.
- O `Curso.java` precisa de getters adicionais que o Miro ainda não colocou. Adicionem:
  - `getDescricao()`, `getDataInicio()`, `getCodigo()`, `getEstado()`
  - `setEstado(int e)` — para mudar o estado do curso
  - `getEstadoTexto()` — opcional, retorna texto legível do estado
- Lembrem de gerar o código NanoID de 10 caracteres ao criar um curso novo.
