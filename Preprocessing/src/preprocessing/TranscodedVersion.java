/*
 * Copyright 2017 Université Nice Sophia Antipolis (member of Université Côte d'Azur), CNRS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package preprocessing;

import java.io.File;

public class TranscodedVersion {
	
	public final String filePath; //Filepath of a file obtained from the encoding process
	public final String fileName; //File name without the full path
	public final int width; //Encoded video width
	public final int height; //Encoded video height
	
	public TranscodedVersion(String filePath, int width, int height) {
		this.filePath = filePath;
		//Retrieving the filename
		File f = new File(this.filePath);
		this.fileName = f.getName();
		this.width = width;
		this.height = height;
	}

}
