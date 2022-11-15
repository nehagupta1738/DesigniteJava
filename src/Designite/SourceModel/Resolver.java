package Designite.SourceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ImportDeclaration;

class Resolver {
	private List<Type> typeList = new ArrayList<>();
	private boolean isParameterized = false;
	private boolean isArray = false;
	private Type arrayType;

	public List<SmType> inferStaticAccess(List<Name> staticFieldAccesses, SmType type) {
		List<SmType> typesOfStaticAccesses = new ArrayList<>();
		for (Name typeName : staticFieldAccesses) {
			if (typeName.resolveBinding() instanceof ITypeBinding) {
				ITypeBinding iType = (ITypeBinding) typeName.resolveBinding();

				if (iType != null && iType.getPackage() != null) {
					SmPackage sm_pkg = findPackage(iType.getPackage().getName().toString(),
							type.getParentPkg().getParentProject());

					if (sm_pkg != null) {
						SmType sm_type = findType(iType.getName().toString(), sm_pkg);
						if (sm_type != null) {
							if (!typesOfStaticAccesses.contains(sm_type)) {
								typesOfStaticAccesses.add(sm_type);
							}
						}
					}
				}
			} else {
				String unresolvedTypeName = typeName.toString().replace("[]", ""); // cover the Array case
				SmType matchedType = manualLookupForUnresolvedType(type.getParentPkg().getParentProject(),
						unresolvedTypeName, type);
				if (matchedType != null) {
					if (!typesOfStaticAccesses.contains(matchedType)) {
						typesOfStaticAccesses.add(matchedType);
					}
				}
			}
		}

		return typesOfStaticAccesses;
	}

	public List<SmMethod> inferCalledMethods(List<MethodInvocation> calledMethods, SmType parentType) {
		List<SmMethod> calledMethodsList = new ArrayList<>();
		for (MethodInvocation method : calledMethods) {
			IMethodBinding imethod = method.resolveMethodBinding();

			if (imethod != null) {
				SmPackage sm_pkg = findPackage(imethod.getDeclaringClass().getPackage().getName().toString(),
						parentType.getParentPkg().getParentProject());
				if (sm_pkg != null) {
					SmType sm_type = findType(imethod.getDeclaringClass().getName().toString(), sm_pkg);
					if (sm_type != null) {
						SmMethod sm_method = findMethod(imethod, sm_type);
						if (sm_method != null)
							calledMethodsList.add(sm_method);
					}
				}
			}
			else {
				Expression exp = method.getExpression();
				if (exp != null) {
					String typeName = exp.toString();
					SmType matchedType = manualLookupForUnresolvedType(parentType.getParentPkg().getParentProject(),
							typeName, parentType);
					if (matchedType != null) {
						parentType.addStaticMethodInvocation(matchedType);
					}
				}
				List<Expression> arguments = new ArrayList<Expression>(method.arguments());
				ListIterator<Expression> itr = arguments.listIterator();

				while (itr.hasNext()) {
					Expression temp = null;
					exp = itr.next();
					String typeName = exp.toString();
					SmType matchedType = manualLookupForUnresolvedType(parentType.getParentPkg().getParentProject(),
							typeName, parentType);
					if (matchedType != null) {
						parentType.addStaticMethodInvocation(matchedType);
					}
					if (exp instanceof MethodInvocation) {
						temp = ((MethodInvocation) exp).getExpression();
						addExpressionArguments(((MethodInvocation) exp).arguments(), itr);
						if (temp != null) {
							itr.add(temp);
						}
					} else if (exp instanceof ClassInstanceCreation) {
						addExpressionArguments(((ClassInstanceCreation) exp).arguments(), itr);
					}
				}

			}
		}
		return calledMethodsList;
	}

	public void addExpressionArguments(List<Expression> newArgumentList,
			ListIterator<Expression> existingArgumentList) {
		for (Expression newArgument : newArgumentList)
			existingArgumentList.add(newArgument);
	}

	private SmPackage findPackage(String packageName, SmProject project) {
		for (SmPackage sm_pkg : project.getPackageList()) {
			if (sm_pkg.getName().equals(packageName)) {
				return sm_pkg;
			}
		}

		return null;
	}

	private SmMethod findMethod(IMethodBinding method, SmType type) {
		String methodName = method.getName().toString();
		int parameterCount = method.getParameterTypes().length;
		boolean sameParameters = true;

		for (SmMethod sm_method : type.getMethodList()) {
			if (sm_method.getName().equals(methodName)) {
				if (sm_method.getParameterList().size() == parameterCount) {
					if (parameterCount == 0) {
						return sm_method;
					}
					for (int i = 0; i < parameterCount; i++) {
						ITypeBinding parameterType = method.getParameterTypes()[i];
						Type typeToCheck = sm_method.getParameterList().get(i).getTypeBinding();
						if (!(parameterType.getName().contentEquals(typeToCheck.toString()))) {
							sameParameters = false;
							break;
						} else if (i == parameterCount - 1) {
							if (sameParameters)
								return sm_method;
						}
					}
				}
			}
		}

		return null;
	}

	public SmType resolveType(Type type, SmProject project) {
		ITypeBinding binding = type.resolveBinding();
		if (binding == null || binding.getPackage() == null) // instanceof String[] returns null package
			return null;
		SmPackage pkg = findPackage(binding.getPackage().getName(), project);
		if (pkg != null) {
			return findType(binding.getName(), pkg);
		}
		return null;
	}

	public TypeInfo resolveVariableType(Type typeNode, SmProject parentProject, SmType callerType) {
		TypeInfo typeInfo = new TypeInfo();
		specifyTypes(typeNode);

		if (isParameterized) {
			for (Type typeOfVar : getTypeList()) {
				inferTypeInfo(parentProject, typeInfo, typeOfVar, callerType);
			}
		} else if (isArray) {
			inferTypeInfo(parentProject, typeInfo, getArrayType(), callerType);
		} else {
			inferTypeInfo(parentProject, typeInfo, typeNode, callerType);
		}
		return typeInfo;
	}

	private void inferTypeInfo(SmProject parentProject, TypeInfo typeInfo, Type typeOfVar, SmType callerType) {
		ITypeBinding iType = typeOfVar.resolveBinding();

		if (iType == null) {
			inferPrimitiveType(parentProject, typeInfo, iType);
			infereParametrized(parentProject, typeInfo, iType);
		} else if (iType.isRecovered()) {
			String unresolvedTypeName = typeOfVar.toString().replace("[]", ""); // cover the Array case
			SmType matchedType = manualLookupForUnresolvedType(parentProject, unresolvedTypeName, callerType);
			if (matchedType != null) {
				manualInferUnresolvedTypeType(typeInfo, matchedType);
			}
		} else {
			inferPrimitiveType(parentProject, typeInfo, iType);
			infereParametrized(parentProject, typeInfo, iType);
		}
	}

	private SmType manualLookupForUnresolvedType(SmProject parentProject, String unresolvedTypeName,
												 SmType callerType) {
		SmType matchedType = null;

		int numberOfDots = new StringTokenizer(" " + unresolvedTypeName + " ", ".").countTokens() - 1;

		if (numberOfDots == 1) {
			unresolvedTypeName = unresolvedTypeName.substring(0, unresolvedTypeName.indexOf("."));
		}
		else if (numberOfDots > 1) {
			String packageName = getPackageName(unresolvedTypeName);
			String typeName = getTypeName(unresolvedTypeName);
			matchedType = findType(typeName, packageName, parentProject);
			if (matchedType != null) {
				return matchedType;
			}
		}

		if ((matchedType = findType(unresolvedTypeName, callerType.getParentPkg())) != null) {
			return matchedType;
		}
		else {
			List<ImportDeclaration> importList = callerType.getImportList();
			for (ImportDeclaration importEntry : importList) {
				matchedType = findType(unresolvedTypeName, getPackageName(importEntry.getName().toString()),
						parentProject);
				if (matchedType != null) {
					return matchedType;
				}
			}
		}
		return null;
	}

	private String getTypeName(String fullTypePath) {
		return fullTypePath.substring(fullTypePath.lastIndexOf(".") + 1);
	}

	private String getPackageName(String fullTypePath) {
		int index = fullTypePath.lastIndexOf('.');
		if (index >= 0)
			return fullTypePath.substring(0, fullTypePath.lastIndexOf('.'));
		else
			return "default";
	}

	private void manualInferUnresolvedTypeType(TypeInfo typeInfo, SmType type) {
		typeInfo.setTypeObj(type);
		typeInfo.setPrimitiveType(false);
	}

	private void inferPrimitiveType(SmProject parentProject, TypeInfo typeInfo, ITypeBinding iType) {
		if (iType != null && iType.isFromSource() && iType.getModifiers() != 0 && !iType.isWildcardType()) {
			SmType inferredType = findType(iType.getName(), iType.getPackage().getName(), parentProject);
			if (inferredType != null) {
				typeInfo.setTypeObj(inferredType);
				typeInfo.setPrimitiveType(false);
			} else {
				typeInfo.setObjPrimitiveType(iType.getName());
				typeInfo.setPrimitiveType(true);
			}
		} else {
			if (iType == null)
				typeInfo.setObjPrimitiveType("wildcard");
			else
				typeInfo.setObjPrimitiveType(iType.getName());
			typeInfo.setPrimitiveType(true);
		}
	}

	private void infereParametrized(SmProject parentProject, TypeInfo typeInfo, ITypeBinding iType) {
		if (iType != null && iType.isParameterizedType()) {
			typeInfo.setParametrizedType(true);
			addNonPrimitiveParameters(parentProject, typeInfo, iType);
			if (hasNonPrimitivePArameters(typeInfo)) {
				typeInfo.setPrimitiveType(false);
			}
		}
	}

	private void addNonPrimitiveParameters(SmProject parentProject, TypeInfo typeInfo, ITypeBinding iType) {
		if (iType.isFromSource() && iType.getModifiers() != 0) {
			SmType inferredBasicType = findType(iType.getName(), iType.getPackage().getName(), parentProject);
			addParameterIfNotAlreadyExists(typeInfo, inferredBasicType);
		}
		for (ITypeBinding typeParameter : iType.getTypeArguments()) {
			if (typeParameter.isParameterizedType()) {
				addNonPrimitiveParameters(parentProject, typeInfo, typeParameter);
			} else {
				if (typeParameter.isFromSource() && typeParameter.getModifiers() != 0) {
					SmType inferredType = findType(typeParameter.getName(), typeParameter.getPackage().getName(),
							parentProject);
					if (inferredType != null) {
						addParameterIfNotAlreadyExists(typeInfo, inferredType);
					}
				}
			}
		}
	}

	private void addParameterIfNotAlreadyExists(TypeInfo typeInfo, SmType inferredType) {
		if (!typeInfo.getNonPrimitiveTypeParameters().contains(inferredType)) {
			typeInfo.addNonPrimitiveTypeParameter(inferredType);
		}
	}

	private boolean hasNonPrimitivePArameters(TypeInfo typeInfo) {
		return typeInfo.getNumOfNonPrimitiveParameters() > 0;
	}

	private SmType findType(String typeName, String packageName, SmProject project) {
		SmPackage pkg = findPackage(packageName, project);
		if (pkg != null) {
			return findType(typeName, pkg);
		}
		return null;
	}

	private SmType findType(String className, SmPackage pkg) {
		for (SmType sm_type : pkg.getTypeList()) {
			if (sm_type.getName().equals(trimParametersIfExist(className))) {
				return sm_type;
			}
		}

		return null;
	}

	private String trimParametersIfExist(String objName) {
		int index = objName.indexOf('<');
		if (index >= 0) {
			return objName.substring(0, index);
		}
		return objName;
	}

	private void specifyTypes(Type type) {
		if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			List<Type> typeArgs = parameterizedType.typeArguments();

			for (int i = 0; i < typeArgs.size(); i++)
				setTypeList(typeArgs.get(i));

		} else if (type.isArrayType()) {
			Type arrayType = ((ArrayType) type).getElementType();
			setArrayType(arrayType);
		}
	}

	private void setTypeList(Type newType) {
		if (newType.isAnnotatable())
			typeList.add(newType);
		else {
			specifyTypes(newType);
		}
	}

	private List<Type> getTypeList() {
		return typeList;
	}

	private Type getArrayType() {
		return arrayType;
	}

	private void setArrayType(Type type) {
		arrayType = type;
	}
}
