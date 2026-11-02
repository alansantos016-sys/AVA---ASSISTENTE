# AVA Assistente

Aplicativo Android em desenvolvimento para apoiar a navegação do aluno no AVA Anhanguera, com autenticação realizada pelo próprio aluno.

## Escopo inicial
- Abrir o portal oficial dentro de um WebView.
- Permitir login do aluno diretamente na página institucional.
- Navegação básica: voltar, atualizar e abrir o AVA.
- Não coletar nem armazenar senha do aluno.
- Planejar apoio para identificar materiais textuais, videoaulas e atividades.

## Limites e segurança
A automação futura deve respeitar os termos da instituição e não contornar mecanismos de segurança, presença ou avaliações. Para questionários avaliativos, a versão inicial oferece suporte de estudo e revisão, mantendo o aluno responsável por responder e enviar.

## Construir
Abra o projeto no Android Studio, sincronize Gradle e compile para um dispositivo Android. A implementação de início está em `app/`.

## Status
Protótipo inicial. A integração e compatibilidade do login WebView ainda precisam ser testadas no dispositivo; alguns provedores podem exigir navegador externo ou fluxo oficial de autenticação.
