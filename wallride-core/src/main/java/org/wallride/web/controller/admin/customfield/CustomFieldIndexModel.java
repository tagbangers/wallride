package org.wallride.web.controller.admin.customfield;

import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomFieldIndexModel extends ArrayList<Map<String, Object>> {
	
	public CustomFieldIndexModel(List<Long> ids, BindingResult bindingResult) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("ids", ids);
		map.put("result", bindingResult.getFieldErrors());
		result.add(map);
		this.addAll(result);
	}
}
