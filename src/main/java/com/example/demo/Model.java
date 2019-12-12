package com.example.demo;

public class Model {

	private Long id;
	private String status;
	private String origem;
	private String destino;

	public Model() {
	}

	public Model(String status, String origem, String destino) {
		this.status = status;
		this.origem = origem;
		this.destino = destino;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	public String getDestino() {
		return destino;
	}

	public void setDestino(String destino) {
		this.destino = destino;
	}

}
