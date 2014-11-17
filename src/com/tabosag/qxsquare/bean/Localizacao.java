package com.tabosag.qxsquare.bean;

public class Localizacao {

	private String cidade;
	private String prefixoPais;
	private String pais;
	private String cep;
	private String estado;
	private Endereco endereco;

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getPrefixoPais() {
		return prefixoPais;
	}

	public void setPrefixoPais(String prefixoPais) {
		this.prefixoPais = prefixoPais;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
}
