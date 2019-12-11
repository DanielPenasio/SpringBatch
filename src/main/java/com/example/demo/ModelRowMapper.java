package com.example.demo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ModelRowMapper implements RowMapper<Model> {

	@Override
	public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
		Model model = new Model();
		model.setStatus(rs.getString("status"));
		model.setOrigem(rs.getString("origem"));
		model.setDestino(rs.getString("destino"));
		return model;
	}

}
