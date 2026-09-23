package br.com.gestaoservicos.orcamento.service;

import br.com.gestaoservicos.orcamento.model.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service public class GeradorPdfOrcamento {
 private static final DateTimeFormatter DATA=DateTimeFormatter.ofPattern("dd/MM/uuuu"); private final ObjectMapper json;
 public GeradorPdfOrcamento(ObjectMapper json){this.json=json;}
 public byte[] gerar(Orcamento o,RevisaoOrcamento r){try(PDDocument d=new PDDocument();ByteArrayOutputStream out=new ByteArrayOutputStream()){
  Escritor e=new Escritor(d); e.titulo("ORCAMENTO");e.linha("Numero: "+o.getNumero()+" / Rev. "+r.getNumeroRevisao());e.linha("Emissao: "+DATA.format(LocalDate.now()));if(r.getValidade()!=null)e.linha("Validade: "+DATA.format(r.getValidade()));
  JsonNode contratado=ler(r.getContratadoSnapshot());e.secao("CONTRATADO");e.linha(campo(contratado,"nomeRazaoSocial","Nao informado"));e.linha(campo(contratado,"documento",""));e.linha(campo(contratado,"enderecoCompleto",""));String logo=campo(contratado,"logoUrl","");if(!logo.isBlank())e.linha("Logo da unidade: "+logo);
  JsonNode contratante=ler(r.getContratanteSnapshot());e.secao("CONTRATANTE");if(contratante==null)e.linha("Proposta sem destinatario");else{e.linha(campo(contratante,"nomeRazaoSocial",""));e.linha(campo(contratante,"documento",""));e.linha(campo(contratante,"endereco",""));for(JsonNode contato:contratante.path("contatos"))e.linha("Contato: "+campo(contato,"nome","")+" "+campo(contato,"telefone","")+" "+campo(contato,"email", ""));}
  e.secao("ITENS");for(ItemRevisaoOrcamento i:r.getItens())e.linha(i.getDescricao()+" - "+i.getQuantidade()+" x "+moeda(i.getValorUnitario())+" = "+moeda(i.getTotal()));
  e.secao("MEMORIA DE CALCULO");e.linha("Subtotal servicos: "+moeda(r.getSubtotalServicos()));e.linha("Subtotal materiais: "+moeda(r.getSubtotalMateriais()));e.linha("Subtotal: "+moeda(r.getSubtotal()));e.linha("Desconto: "+moeda(r.getDescontoTotal()));e.linha("Acrescimo: "+moeda(r.getAcrescimoTotal()));e.linha("TOTAL: "+moeda(r.getTotalFinal()));
  e.secao("PAGAMENTO");e.linha(pagamentos(r.getPagamentosSnapshot()));if(r.getCondicoesPagamento()!=null)e.linha("Condicoes: "+r.getCondicoesPagamento());if(r.getObservacoesComerciais()!=null){e.secao("OBSERVACOES");e.linha(r.getObservacoesComerciais());}if(r.isExibirAssinatura()){e.secao("ACEITE DO CONTRATANTE");e.linha("Assinatura: ______________________________________________");}e.fechar();d.save(out);return out.toByteArray();
 }catch(IOException ex){throw new IllegalStateException("Nao foi possivel gerar PDF do orcamento",ex);}}
 private JsonNode ler(String valor){if(valor==null)return null;try{return json.readTree(valor);}catch(Exception ex){throw new IllegalStateException("Snapshot invalido",ex);}}
 private String campo(JsonNode n,String chave,String padrao){return n==null?padrao:n.path(chave).asText(padrao);} private String pagamentos(String valor){JsonNode ps=ler(valor);if(ps==null||!ps.isArray()||ps.isEmpty())return "Nao informado";List<String> ls=new ArrayList<>();for(JsonNode p:ps)ls.add(campo(p,"nomeExibicao","")+": "+campo(p,"instrucoes",""));return String.join("; ",ls);}private String moeda(BigDecimal v){return "R$ "+v.setScale(2).toPlainString();}
 private static class Escritor {private final PDDocument d;private final PDFont fonte=new PDType1Font(Standard14Fonts.FontName.HELVETICA);private PDPageContentStream tela;private float y;Escritor(PDDocument d)throws IOException{this.d=d;novaPagina();}void titulo(String s)throws IOException{escrever(s,16);y-=8;}void secao(String s)throws IOException{y-=10;escrever(s,12);}void linha(String s)throws IOException{for(String p:quebrar(s,92))escrever(p,9);}void fechar()throws IOException{tela.close();}private void novaPagina()throws IOException{if(tela!=null)tela.close();PDPage p=new PDPage();d.addPage(p);tela=new PDPageContentStream(d,p);y=770;}private void escrever(String s,int t)throws IOException{if(y<45)novaPagina();tela.beginText();tela.setFont(fonte,t);tela.newLineAtOffset(42,y);tela.showText(ascii(s));tela.endText();y-=t+4;}private List<String> quebrar(String s,int limite){List<String> ls=new ArrayList<>();String a="";for(String p:s.split("\\s+")){if((a+" "+p).length()>limite){if(!a.isEmpty())ls.add(a);a=p;}else a=a.isEmpty()?p:a+" "+p;}if(!a.isEmpty())ls.add(a);return ls;}private String ascii(String s){return java.text.Normalizer.normalize(s,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^\\x20-\\x7E]","?");}}
}
