package com.example.demo;

import org.springframework.batch.item.ItemProcessor;

public class PoolingItemProcessor implements ItemProcessor<Model, Model> {

	
	@Override
	public Model process(Model model) throws Exception {
		
//		Model modelProcess = new Model(model.getStatus(),model.getOrigem(),model.getDestino());
		System.out.println("");
		System.out.println("------------------------------------------------------");
		System.out.println("Saída do método process da classe PoolingItemProcessor");
		System.out.println("------------------------------------------------------");
		System.out.println("ID: " + model.getId() + 
							" - Status: " + model.getStatus() + 
							" - Origem: " + model.getOrigem() + 
							" - Destino: "+ model.getDestino());
		System.out.println("------------------------------------------------------");
		
		
		return model;
	}

}
