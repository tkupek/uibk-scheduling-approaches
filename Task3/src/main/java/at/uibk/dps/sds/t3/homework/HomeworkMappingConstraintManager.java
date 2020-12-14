package at.uibk.dps.sds.t3.homework;

import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.mapping.MappingConstraintManager;
import net.sf.opendse.model.properties.ProcessPropertyService.MappingModes;

public class HomeworkMappingConstraintManager implements MappingConstraintManager{

	protected final HomeworkMappingEncoding homeWorkMappingEncoding = new HomeworkMappingEncoding();
	
	@Override
	public MappingConstraintGenerator getMappingConstraintGenerator(MappingModes mappingMode) {
		return homeWorkMappingEncoding;
	}
}
